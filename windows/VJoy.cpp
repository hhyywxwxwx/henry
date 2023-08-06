//// VJoy.cpp : Defines the entry point for the console application.
////
#include "stdafx.h"

#include <iostream>
#include <thread>
#include <atomic>
#include <windows.h>
#include <WinSock.h>
//#pragma comment(lib,"ws2_32.lib")
//
//#include "VJoy.h"
//
//using namespace std;
//
//JOYSTICK_STATE m_joyState[2] = { 0 };
//atomic_bool isRunning;
//
//int main_2(int argc, char* argv[])
//{
//	WORD wVersion = MAKEWORD(1, 1);
//	WSADATA wsdData;
//	if (WSAStartup(wVersion, &wsdData) != 0) {
//		cout << "WSAStartup Error\n";
//		return -1;
//	}
//	SOCKET socket_send = socket(AF_INET, SOCK_DGRAM, 0);
//	if (socket_send == INVALID_SOCKET) {
//		cout << "socket Error\n";
//	}
//	struct sockaddr_in address;
//	address.sin_family = AF_INET;
//	address.sin_port = htons(8458);
//	address.sin_addr.S_un.S_addr = inet_addr("192.168.0.110");
//	for (int i = 0; i < 10; ++i) {
//		char data[10];
//		sprintf(data, "hello");
//		int err = sendto(socket_send, data, sizeof(data), 0, (sockaddr*)&address, sizeof(address));
//	}
//	WSACleanup();
//	return 0;
//}
