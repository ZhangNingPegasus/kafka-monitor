A kafka monitor and management tool and provide several functionals such as topic, consumer, offsets, lags, records trace, alerm, big screen reports and etc.

Supported on kafka version: 2.2.x and above.

Supported platform: Linux, Windows, Mac OS.

Supported JDK: jdk8+

<h3>HOW TO Install</h3>
1). install MySql 5.7 or above.<br/>
2). set several environment varaibles.<br/>
   &nbsp;&nbsp;&nbsp;&nbsp;a. kafka_monitor_port <br/>
   &nbsp;&nbsp;&nbsp;&nbsp;b. zookeeper_connect <br/>
   &nbsp;&nbsp;&nbsp;&nbsp;c. db_host<br/>
   &nbsp;&nbsp;&nbsp;&nbsp;d. db_port<br/>
   &nbsp;&nbsp;&nbsp;&nbsp;e. db_username<br/>
   &nbsp;&nbsp;&nbsp;&nbsp;f. db_password<br/>

   &nbsp;&nbsp;&nbsp;&nbsp;take an example in Linux as below:<br/>

   &nbsp;&nbsp;&nbsp;&nbsp;vim /etc/profile<br/>
   &nbsp;&nbsp;&nbsp;&nbsp;export kafka_monitor_port=80<br/>
   &nbsp;&nbsp;&nbsp;&nbsp;export zookeeper_connect=192.168.6.166:2181,192.168.6.167:2181<br/>
   &nbsp;&nbsp;&nbsp;&nbsp;export db_host=127.0.0.1<br/>
   &nbsp;&nbsp;&nbsp;&nbsp;export db_port=3306<br/>
   &nbsp;&nbsp;&nbsp;&nbsp;export db_username=root<br/>
   &nbsp;&nbsp;&nbsp;&nbsp;export db_password=root<br/>


 3). nohup java -jar xxx.jar &<br/><br/>

Default Account: admin<br/>
Default Password: admin<br/>

Any questions or concerns please feel free to contact me by email: 349409664@qq.com

Here are some Kafka-Monitor system screenshots:
<img src="https://raw.githubusercontent.com/ZhangNingPegasus/kafka-monitor/master/1.png"/>
<img src="https://raw.githubusercontent.com/ZhangNingPegasus/kafka-monitor/master/2.png"/>
<img src="https://raw.githubusercontent.com/ZhangNingPegasus/kafka-monitor/master/3.png"/>
<img src="https://raw.githubusercontent.com/ZhangNingPegasus/kafka-monitor/master/4.png"/>
<img src="https://raw.githubusercontent.com/ZhangNingPegasus/kafka-monitor/master/5.png"/>
<img src="https://raw.githubusercontent.com/ZhangNingPegasus/kafka-monitor/master/6.png"/>
<img src="https://raw.githubusercontent.com/ZhangNingPegasus/kafka-monitor/master/7.png"/>
<img src="https://raw.githubusercontent.com/ZhangNingPegasus/kafka-monitor/master/8.png"/>
<img src="https://raw.githubusercontent.com/ZhangNingPegasus/kafka-monitor/master/9.png"/>
<img src="https://raw.githubusercontent.com/ZhangNingPegasus/kafka-monitor/master/10.png"/>
<img src="https://raw.githubusercontent.com/ZhangNingPegasus/kafka-monitor/master/11.png"/>
<img src="https://raw.githubusercontent.com/ZhangNingPegasus/kafka-monitor/master/12.png"/>
<img src="https://raw.githubusercontent.com/ZhangNingPegasus/kafka-monitor/master/13.png"/>
<img src="https://raw.githubusercontent.com/ZhangNingPegasus/kafka-monitor/master/14.png"/>
<img src="https://raw.githubusercontent.com/ZhangNingPegasus/kafka-monitor/master/15.png"/>
<img src="https://raw.githubusercontent.com/ZhangNingPegasus/kafka-monitor/master/16.png"/>
<img src="https://raw.githubusercontent.com/ZhangNingPegasus/kafka-monitor/master/17.png"/>
<img src="https://raw.githubusercontent.com/ZhangNingPegasus/kafka-monitor/master/18.png"/>
<img src="https://raw.githubusercontent.com/ZhangNingPegasus/kafka-monitor/master/19.png"/>
<img src="https://raw.githubusercontent.com/ZhangNingPegasus/kafka-monitor/master/20.png"/>

