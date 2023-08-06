# dji vjoy
## 控制过程
![](imgs/数据流.png)

移动端通过大疆SDK收集遥控器摇杆数据(Stick)、按键数据(C1Button、GoHomeButton)以及左侧滚轮数据(leftDial)，通过UDP发送至PC端。

PC端从移动端获取遥控器数据，通过VJoy SDK模拟虚拟手柄，将摇杆数据映射至VJoy Virtual Joystick[0]，再通过x360ce模拟成xbox360手柄输入大疆飞行模拟(DFS)；其余数据根据功能映射至键盘事件，触发DFS事件，即云台旋转、自动返航和切换飞行模式。