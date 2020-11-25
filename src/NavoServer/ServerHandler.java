package NavoServer;

import Util.JsonParser;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.json.simple.JSONObject;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    private static final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("handlerAdded of [SERVER]");
//        Channel incoming = ctx.channel();
//        for (Channel channel : channelGroup) {
//            //사용자가 추가되었을 때 기존 사용자에게 알림
//            channel.write("[SERVER] - " + incoming.remoteAddress() + "has joined!\n");
//        }
//        channelGroup.add(incoming);
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 사용자가 접속했을 때 서버에 표시.
        System.out.println("User Access!");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println("handlerRemoved of [SERVER]");
//        Channel incoming = ctx.channel();
//        for (Channel channel : channelGroup) {
//            //사용자가 나갔을 때 기존 사용자에게 알림
//            channel.write("[SERVER] - " + incoming.remoteAddress() + "has left!\n");
//            channelGroup.remove(incoming);
//        }

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        //Event.logout(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("channelRead of [SERVER]" + msg);
        JSONObject json = JsonParser.createJson((String)msg);

        switch ((String)json.get("Header")) { // Header를 보고 로직 분류.
            case "Auth":
                //Auth.Auth(ctx,json.get("Body").toString(), Integer.parseInt(json.get("Function").toString())); //Body 안의 정보, Function 번호
                Auth.auth(ctx, JsonParser.createJson(json.get("Body").toString()), Integer.parseInt(json.get("Function").toString()));

                // 클래스의 생성자에 메소드 구분 로직을 넣으면 어떨까요?
                // new Auth(ctx, JsonParser.createJson(json.get("Body").toString()), Integer.parseInt(json.get("Function").toString()));

                break;
            case "Update":
                new Update(ctx, JsonParser.createJson(json.get("Body").toString()), Integer.parseInt(json.get("Function").toString()));
                break;
            case "Event":
                //Event.Event(ctx,json.get("Body").toString());
                Event.event(ctx, JsonParser.createJson(json.get("Body").toString()), Integer.parseInt(json.get("Function").toString()), Integer.parseInt(json.get("roomCode").toString()));
                break;
        }
    }
}
