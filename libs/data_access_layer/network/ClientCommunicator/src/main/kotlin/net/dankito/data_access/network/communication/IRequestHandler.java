package net.dankito.data_access.network.communication;

import net.dankito.data_access.network.communication.callback.RequestHandlerCallback;
import net.dankito.data_access.network.communication.message.Request;


public interface IRequestHandler {

  void handle(Request request, RequestHandlerCallback callback);

}
