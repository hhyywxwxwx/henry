#include "stdafx.h"
#include "Utils.h"

vector<string> getHostIP() {
	const int hostnameLen = 100;
	char hostname[hostnameLen] = { '\0' };
	gethostname(hostname, hostnameLen);
	struct hostent* p = gethostbyname(hostname);
	if (p == NULL) {
		return {};
	}
	vector<string> ret;
	for (int i = 0; p->h_addr_list[i] != 0; i++)
	{
		struct in_addr in;
		memcpy(&in, p->h_addr_list[i], sizeof(struct in_addr));
		ret.push_back(inet_ntoa(in));
	}
	return ret;
}

SOCKET initSocket(u_short port) throw(exception) {
	SOCKET sock = socket(AF_INET, SOCK_DGRAM, 0);
	if (sock == INVALID_SOCKET) {
		throw exception("socket() error");
	}
	SOCKADDR_IN address;
	address.sin_addr.S_un.S_addr = htonl(INADDR_ANY);
	address.sin_family = AF_INET;
	address.sin_port = htons(port);
	bind(sock, (sockaddr*)&address, sizeof(address));
	return sock;
}

void pressKey(BYTE key) {
	keybd_event(key, 0, 0, 0);
}
void releaseKey(BYTE key) {
	keybd_event(key, 0, KEYEVENTF_KEYUP, 0);
}