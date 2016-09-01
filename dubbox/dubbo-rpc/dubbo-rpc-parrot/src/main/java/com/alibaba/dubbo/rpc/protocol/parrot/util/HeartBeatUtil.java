package com.alibaba.dubbo.rpc.protocol.parrot.util;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;

import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.remoting.exchange.Response;

public class HeartBeatUtil {
   

    public static Request  parrotHeartBeat2Request(ChannelBuffer buffer,DefaultParrotContext parrotContext,TMessage message) throws TException{
       /* RandomAccessByteArrayOutputStream bos = new RandomAccessByteArrayOutputStream(1024);
        TIOStreamTransport transport = new TIOStreamTransport(bos);
        TBinaryProtocol protocol = new TBinaryProtocol(transport);
        protocol.writeMessageBegin(new TMessage(ParrotConstants.heartbeat_make, TMessageType.REPLY, message.seqid) );
        protocol.writeMessageEnd();
        protocol.getTransport().flush();
        byte[] bytes = new byte[4];
        TFramedTransport.encodeFrameSize(bos.size(), bytes);
        buffer.writeBytes(bytes);
        buffer.writeBytes(bos.toByteArray());*/
        Request req = new Request(message.seqid);
        req.setVersion( "2.0.0" );
        req.setTwoWay( true );
        req.setEvent( Request.HEARTBEAT_EVENT );
        req.getRequestParams().put(ParrotConstants.PARROTCONTEXT, parrotContext);
        return req;
        
    }
    public static void  response2ParrotHeartBeat( Response response,TProtocol outProtocol) throws TException{
        DefaultParrotContext context = (DefaultParrotContext) response.getRequest().getRequestParams().get(ParrotConstants.PARROTCONTEXT);
        DefaultParrotContextUtil.encodeContext(context, outProtocol);
        outProtocol.writeMessageBegin(new TMessage(ParrotConstants.heartbeat_make, TMessageType.REPLY, 0) );
        outProtocol.writeMessageEnd();
    }
    public static  boolean  clientReceiveHeartbeat( DefaultParrotContext defaultParrotContext,TProtocol inProtocol,TProtocol outProtocol) throws TException{
        
        boolean isOK=false;
        String isHeartbeat=defaultParrotContext.getContext(DefaultParrotContext.ISHEARTBEAT);
        if(isHeartbeat!=null&&isHeartbeat.equals("true")){
            TMessage tMessage=inProtocol.readMessageBegin();
            inProtocol.readMessageEnd();
            if(tMessage.name.equals(ParrotConstants.heartbeat_make)&&tMessage.type==TMessageType.REPLY){
            	isOK=true;
            }else if(tMessage.name.equals(ParrotConstants.heartbeat_make)&&tMessage.type==TMessageType.EXCEPTION){
            	isOK=false;
            }
        }
        
        return isOK;
    }
}
