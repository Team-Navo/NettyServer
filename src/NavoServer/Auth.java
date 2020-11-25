package NavoServer;

import Util.JsonParser;
import dev.game.netty.database.DatabaseConnection;
import dev.game.netty.database.table.User;
import io.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONObject;

public class Auth {
    static DatabaseConnection db = DatabaseConnection.getConnector();
    private static JSONObject header;
    private static JSONObject body;

    public static void Auth(ChannelHandlerContext ctx, String msg) {
        try {
            System.out.println(msg);

            JSONObject json = JsonParser.createJson((msg));
            header = new JSONObject();
            body = new JSONObject();

            header.put("Header","Auth");
            body.put("Function",json.get("Function"));

            if(json.get("Function").equals("1")) {
                login(ctx,json);
            }
            // 회원가입
            else if(json.get("Function").equals("2")) {
                create(ctx,json);
            }
            // id 찾기
            else if(json.get("Function").equals("3")) {
                findID(ctx,json);
            }
            // pw 찾기
            else if(json.get("Function").equals("4")) {
                findPW(ctx,json);
            } else {
                body.replace("Function","-1");
                body.put("result","[WRONG] 잘못된 데이터");

                header.put("Body", body);
                ctx.writeAndFlush(header.toJSONString()+"\r\n");
                System.out.println("[WRONG] 잘못된 데이터");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void login(ChannelHandlerContext ctx, JSONObject json) {
        User user = new User();
        user.setId((String)json.get("id"));
        user.setPw((String)json.get("pw"));

        if(db.userLogin(user)) {
            body.put("result","SUCCESS");
            header.put("Body",body);
            ctx.writeAndFlush(header.toJSONString()+"\r\n");
            System.err.println("[SUCCESS] 로그인");
        } else {
            body.put("result","FAIL");
            header.put("Body",body);
            ctx.writeAndFlush(header.toJSONString()+"\r\n");
            System.err.println("[FAIL] 로그인");
        }
    }

    public static void findID(ChannelHandlerContext ctx, JSONObject json) {
        try {
            User user = new User();
            user.setName((String)json.get("name"));
            user.setBirth((String)json.get("birth"));
            String result = db.findID(user);

            if (result != null) {
                body.put("result",result);
                header.put("Body",body);

                ctx.writeAndFlush(header.toJSONString()+"\r\n");
                System.out.println("[SUCCESS] ID 찾기 : " + result);
            } else {
                body.put("result","FAIL");
                header.put("Body",body);

                ctx.writeAndFlush(header.toJSONString()+"\r\n");
                System.err.println("[FAIL] ID 찾기");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void findPW(ChannelHandlerContext ctx, JSONObject json) {
        try {
            User user = new User();
            user.setId((String)json.get("id"));
            user.setName((String)json.get("name"));
            String result = db.findPW(user);

            if (result != null) {
                body.put("result",result);
                header.put("Body",body);

                ctx.writeAndFlush(header.toJSONString()+"\r\n");
                System.out.println("[SUCCESS] PW 찾기 : " + result);
            } else {
                body.put("result","FAIL");
                header.put("Body",body);
                ctx.writeAndFlush(header.toJSONString()+"\r\n");
                System.err.println("[FAIL] PW 찾기");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void create(ChannelHandlerContext ctx, JSONObject json) {
        try {
            User user = new User();
            user.setName((String)json.get("name"));
            user.setBirth((String)json.get("birth"));
            user.setPhone((String)json.get("phone"));
            user.setId((String)json.get("id"));
            user.setPw((String)json.get("pw"));

            if (db.createUser(user)) {
                body.put("result","SUCCESS");
                header.put("Body",body);

                ctx.writeAndFlush(header.toJSONString()+"\r\n");
                System.out.println("[SUCCESS] 회원가입");
            } else {
                body.put("result","FAIL");
                header.put("Body",body);
                ctx.writeAndFlush(header.toJSONString()+"\r\n");
                System.err.println("[FAIL] 회원가입");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
