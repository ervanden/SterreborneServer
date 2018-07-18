package sterreborneserver;

import java.util.ArrayList;


public interface WSServerListener {

    ArrayList<String> onClientRequest(String clientID, String request);

}

