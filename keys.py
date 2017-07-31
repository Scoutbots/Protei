import curses
import paho.mqtt.publish as publish

port = 1883 # Default port for unencrypted MQTT
#hostname = "iot.eclipse.org" # Sandbox broker
#hostname = "broker.hivemq.com"

hostname="HOST"

topic_far="mar"

#topic_2="mar"

topic_2="helloout"

QOS=2

stdscr = curses.initscr()
curses.cbreak()
stdscr.keypad(1)

stdscr.addstr(0,10,"Hit 'q' to quit")
stdscr.refresh()

key = ''
while key != ord('q'):
    key = stdscr.getch()
    stdscr.addch(20,25,key)
    stdscr.refresh()

    if key == curses.KEY_UP:
        stdscr.addstr(2, 20, "Up")
        publish.single(topic_far, payload="u",qos=QOS,hostname=hostname,port=port)

    elif key == curses.KEY_DOWN:
        stdscr.addstr(3, 20, "Down")
        publish.single(topic_far, payload="dow",qos=QOS,hostname=hostname,port=port)
	
    elif key == curses.KEY_RIGHT:
        stdscr.addstr(2, 25, "Right")
        publish.single(topic_2, payload="r",qos=QOS,hostname=hostname,port=port)

    elif key == curses.KEY_LEFT:
        stdscr.addstr(2, 15, "Left")
        publish.single(topic_2, payload="l",qos=QOS,hostname=hostname,port=port)

    '''
     elif key == "a":
        stdscr.addstr(2, 15, "Down")
        publish.single(topic_2, payload="r",qos=1,hostname=hostname,port=port)
     '''	






curses.endwin()
