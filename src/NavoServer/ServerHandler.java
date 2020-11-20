package NavoServer;

import Database.DatabaseConnection;
import Database.User;
import Util.JsonParser;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.json.simple.JSONObject;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    private static final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private DatabaseConnection db = DatabaseConnection.getConnector();
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("handlerAdded of [SERVER]");
        Channel incoming = ctx.channel();
        for (Channel channel : channelGroup) {
            //사용자가 추가되었을 때 기존 사용자에게 알림
            channel.write("[SERVER] - " + incoming.remoteAddress() + "has joined!\n");
        }
        channelGroup.add(incoming);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 사용자가 접속했을 때 서버에 표시.
        System.out.println("User Access!");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println("handlerRemoved of [SERVER]");
        Channel incoming = ctx.channel();
        for (Channel channel : channelGroup) {
            //사용자가 나갔을 때 기존 사용자에게 알림
            channel.write("[SERVER] - " + incoming.remoteAddress() + "has left!\n");
        }
        channelGroup.remove(incoming);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String message = null;
        message = (String)msg;
        System.out.println("channelRead of [SERVER]" +  message);
        JSONObject json = JsonParser.createJson(message);
        Channel incoming = ctx.channel();

        //로그인
        if(json.get("Header").equals("LOGIN")) { // 파싱 데이터의 "Header"가 "login"일 떄
            User user = new User(); // 유저 객체 생성
            user.setId((String) json.get("id"));
            user.setPw((String) json.get("pw"));

            if (db.userLogin(user)) {
                incoming.writeAndFlush("SUCCESS"+"\n");
            } else {
                incoming.writeAndFlush("FAIL"+"\n");
                System.err.println("[FAIL] 로그인");
            }
        }
        // 회원가입
        else if(json.get("Header").equals("CREATE")) {
            User user = new User();
            user.setName((String)json.get("name"));
            user.setBirth((String)json.get("birth"));
            user.setPhone((String)json.get("phone"));
            user.setId((String)json.get("id"));
            user.setPw((String)json.get("pw"));

            if (db.createUser(user)) {
                incoming.writeAndFlush("SUCCESS"+"\n");
                System.out.println("[SUCCESS] 회원가입");
            } else {
                incoming.writeAndFlush("FAIL"+"\n");
                System.err.println("[FAIL] 회원가입");
            }
        }
        // id 찾기
        else if(json.get("Header").equals("ID")) {
            User user = new User();
            user.setName((String)json.get("name"));
            user.setBirth((String)json.get("birth"));

            String result = db.findID(user);
            if ( result != null) {
                incoming.writeAndFlush(result+"\n");
                System.out.println("[SUCCESS] ID 찾기 : " + result);
            } else {
                incoming.writeAndFlush("FAIL"+"\n");
                System.err.println("[FAIL] ID 찾기");
            }
        }
        // pw 찾기
        else if(json.get("Header").equals("PW")) {
            User user = new User();
            user.setId((String)json.get("id"));
            user.setName((String)json.get("name"));
            String result = db.findPW(user);
            if (result != null) {
                incoming.writeAndFlush(result+"\n");
                System.out.println("[SUCCESS] PW 찾기 : " + result);
            } else {
                incoming.writeAndFlush("FAIL"+"\n");
                System.err.println("[FAIL] PW 찾기");
            }
        }
        else {
            incoming.writeAndFlush("[WRONG] 잘못된 데이터"+"\n");
            System.out.println("[WRONG] 잘못된 데이터");
        }
    }
}
