#include "stdafx.h"

#include <iostream>
#include <vector>
#include <string>
#include <thread>
#include <atomic>
#include <chrono>
#include <mutex>
#include <windows.h>
#include <WinSock.h>
#pragma comment(lib,"ws2_32.lib")

#include "VJoy.h"
#include "Utils.h"

using namespace std;

JOYSTICK_STATE m_joyState[2] = { 0 };

bool isRunning = false;

//使用互斥量解决的生产者-消费者问题，需要改进
constexpr int BUFFER_CAPACITY = 512;
JoyValue buffer[BUFFER_CAPACITY] = { 0 };
int bufferFront, bufferBack;
int bufferSize;
timed_mutex bufferLock;

void producer() {
	SOCKET sock;
	SOCKADDR_IN addressClient;
	fd_set readFds;
	timeval timeout = { 5, 0 };
	char buf[200];
	int len = sizeof(SOCKADDR);

	try {
		sock = initSocket(8456);
	}
	catch (exception& e) {
		cout << e.what() << endl;
		return;
	}
	while (isRunning) {
		//通过socket获取摇杆数据
		FD_ZERO(&readFds);
		FD_SET(sock, &readFds);
		int n = select(0, &readFds, NULL, NULL, &timeout);
		if (n > 0) {
			int recvLen = recvfrom(sock, buf, 200, 0, (SOCKADDR*)&addressClient, &len);
			if (recvLen == 1) {//test inform
				cout << "网络正常\n";
			} else {
				while (isRunning) {
					if (bufferLock.try_lock_for(chrono::milliseconds(500))) {
						if (bufferSize >= BUFFER_CAPACITY) {
							bufferLock.unlock();
						}
						else {
							buffer[bufferBack].leftHorizontal = (buf[0] << 8) | (buf[1] & 0xff);
							buffer[bufferBack].leftVertical = (buf[2] << 8) | (buf[3] & 0xff);
							buffer[bufferBack].rightHorizontal = (buf[4] << 8) | (buf[5] & 0xff);
							buffer[bufferBack].rightVertical = (buf[6] << 8) | (buf[7] & 0xff);
							buffer[bufferBack].leftDial = (buf[8] << 8) | (buf[9] & 0xff);
							buffer[bufferBack].C1isClicked = (buf[10] == 0) ? false : true;
							buffer[bufferBack].goHomeisClicked = (buf[11] == 0) ? false : true;
							bufferBack = (bufferBack + 1) % BUFFER_CAPACITY;
							++bufferSize;
							bufferLock.unlock();
							break;
						}
					}
				}
			}
		}
	}
	closesocket(sock);
	cout << "producer exit\n";
}
void consumer() {
	constexpr int REBOOT_NUM = 512;
	int rebootCounter = 0;
	VJoy_Initialize((PCHAR)"", (PCHAR)"");
	while (isRunning) {
		INT16 leftDialValue = 0;
		bool C1 = false, goHome = false;
		bool valid = false;
		if (bufferLock.try_lock_for(chrono::milliseconds(500))) {
			if (bufferSize > 0) {
				m_joyState[0].XAxis = buffer[bufferFront].leftHorizontal / 660.0 * VJOY_AXIS_MAX;
				m_joyState[0].YAxis = -buffer[bufferFront].leftVertical / 660.0 * VJOY_AXIS_MAX;
				m_joyState[0].ZAxis = buffer[bufferFront].rightHorizontal / 660.0 * VJOY_AXIS_MAX;
				m_joyState[0].ZRotation = buffer[bufferFront].rightVertical / 660.0 * VJOY_AXIS_MAX;
				leftDialValue = buffer[bufferFront].leftDial;
				C1 = buffer[bufferFront].C1isClicked;
				goHome = buffer[bufferFront].goHomeisClicked;
				valid = true;
				bufferFront = (bufferFront + 1) % BUFFER_CAPACITY;
				--bufferSize;
			}
			bufferLock.unlock();

			if (rebootCounter >= REBOOT_NUM) {
				VJoy_Shutdown();
				VJoy_Initialize((PCHAR)"", (PCHAR)"");
				rebootCounter = 0;
			}
			++rebootCounter;

			if (valid == false) {
				continue;
			}

			VJoy_UpdateJoyState(0, &m_joyState[0]);

			if (leftDialValue > 0) {
				pressKey('R');
				releaseKey('F');
			}
			else if(leftDialValue < 0){
				pressKey('F');
				releaseKey('R');
			}
			else {
				releaseKey('R');
				releaseKey('F');
			}

			if (C1) {
				pressKey('V');
				releaseKey('V');
			}

			if (goHome) {
				pressKey('O');
				releaseKey('O');
			}
		}
	}
	VJoy_Shutdown();
	cout << "consumer exit\n";
}

int main(int argc, char* argv[])
{
	WORD wVersion = MAKEWORD(1, 1);
	WSADATA wsdData;
	if (WSAStartup(wVersion, &wsdData) != 0) {
		cout << "WSAStartup Error\n";
		return -1;
	}

	vector<string> IPs = getHostIP();
	cout << "本机IP:\n";
	for (auto s : IPs) {
		cout << s << endl;
	}

	cout << "C1(Fn)键用于切换飞行模式(姿态模式、运动模式等)\n";
	cout << "返航键用于返航\n";
	cout << "输入 \"exit\" 以结束\n";

	//初始化缓冲区
	bufferFront = 0;
	bufferBack = 0;
	bufferSize = 0;
	
	isRunning = true;
	thread threadP(producer);
	thread threadC(consumer);
	
	while (true) {
		string in;
		cin >> in;
		if (in == "exit") {
			break;
		}
		else {
			cout << "输入 \"exit\" 以结束\n";
		}
	}
	cout << "正在结束\n";
	isRunning = false;
	threadP.join();
	threadC.join();

	WSACleanup();
	return 0;
}
