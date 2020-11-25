package NavoServer;

import Repository.Crewmate;
import Repository.Room;
import Util.JsonParser;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@SuppressWarnings("unchecked")
public class InGameThread implements Runnable{

    Socket clientSocket;
    BufferedReader in;
    PrintWriter out;

    public InGameThread(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    @Override
    public void run() {
        int i = 0;
        Room room = new Room(-1);
        JSONObject roomInfoJson;
        boolean isThread = true;
        while(isThread) {
            try {
                String recvData = in.readLine();

                //System.out.println("Client: " + recvData);

                JSONObject requestJson = JsonParser.createJson(recvData); // 클라이언트에게 받은 데이터를 JSON 으로 파싱
                if (requestJson != null) {
                    roomInfoJson = new JSONObject();
                    if(requestJson.get("Header").equals("LOGOUT")){ // 로그아웃 할 때
                        //LoginThread  loginThread = new LoginThread(clientSocket);
                        //new Thread(loginThread).start();
                        System.out.println("[LOGOUT]");
                        //isThread=false;
                    }
                    else if(requestJson.get("Header").equals("ENTER")){ // 처음 입장
                        room = Room.getRoom(); // 방을 받아옴
                        roomInfoJson.put("code", room.getRoomCode()); // 방 코드 입력
                        JSONObject crewmatesJson = new JSONObject(); // 방에 있는 유저들 정보 담을 Json


                        for (Crewmate crewmate : room.getCrewmates()){ // 기존 방에 있던 크루원들 초기 정보
                            crewmatesJson.put(room.getCrewmates().indexOf(crewmate), crewmate.getInitCrewmateJson()); // 제이슨으로 받아서 0, 1, 2, 3, 4로 번호 매겨서 제이슨 생성
                        }
                        roomInfoJson.put("crewmates", crewmatesJson); // 기존 크루원들 정보 받아서 업데이트

                        //room.getCrewmates().add(new Crewmate(requestJson.get("owner").toString())); // 처음 입장한 유저가 업데이트할 crewmate 생성

                        //System.out.println(roomInfoJson.toJSONString());

                        out.println(roomInfoJson.toJSONString()); // 방 초기 정보 전송
                    }
                    else if(requestJson.get("Header").equals("UPDATE") && room.getRoomCode() != -1){
                        room.update(requestJson); // 방에 크루메이트 정보 업데이트

                        roomInfoJson.put("code", room.getRoomCode()); // 방번호 입력
                        JSONObject crewmatesJson = new JSONObject(); // 크루메이트들 담을 Json

                        for (Crewmate crewmate : room.getCrewmates()){ // 기존 방에 있던 크루원들 초기 정보
                            crewmatesJson.put(room.getCrewmates().indexOf(crewmate), crewmate.getInitCrewmateJson()); // 크루메이트 담음
                        }
                        crewmatesJson.put("crewmates_size", room.getCrewmates().size()); // 방인원 현재 사이즈
                        roomInfoJson.put("crewmates", crewmatesJson); // 크루메이트들 정보
                        //System.out.println(roomInfoJson);
                        out.println(roomInfoJson.toJSONString()); // 방 정보 클라이언트에 전송
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    if(clientSocket != null)
                        clientSocket.close();
                    in.close();
                    out.close();
                } catch (IOException ioE) {
                    ioE.printStackTrace();
                }
                isThread = false;
            }
        }
    }
}