#pragma once
#include <vector>
#include <string>
#include <exception>
#include <windows.h>
#include <WinSock.h>
#include <iostream>

using namespace std;

vector<string> getHostIP();
SOCKET initSocket(u_short port) throw(exception);

void pressKey(BYTE key);
void releaseKey(BYTE key);

struct JoyValue {
	INT16 leftVertical, leftHorizontal;
	INT16 rightVertical, rightHorizontal;
	INT16 leftDial;
	bool C1isClicked, goHomeisClicked;
};