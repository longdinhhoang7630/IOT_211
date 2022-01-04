import serial.tools.list_ports
import random
import time
import sys
from Adafruit_IO import MQTTClient
import pyowm
from pyowm.commons.exceptions import NotFoundError
#ParseAPIResponseError

APIKEY='c550b8b17b0b764ce277381fbae922f0'                 
OpenWMap=pyowm.OWM(APIKEY)                 


AIO_FEED_ID = "bbc-led"
AIO_USERNAME = "dhl2k"
AIO_KEY = "aio_XXJl85fsLoDxBsaKvfnQqBs6v04Y"

def  connected(client):
    print("Ket noi thanh cong...")
    client.subscribe(AIO_FEED_ID)
    client.subscribe("bbc-temp")
    client.subscribe("bbc-humid")

def  subscribe(client , userdata , mid , granted_qos):
    print("Subcribe thanh cong...")

def  disconnected(client):
    print("Ngat ket noi...")
    sys.exit (1)

next_index=0
def  message(client , feed_id , payload):
    print("Nhan du lieu: " + payload + " from feed " + feed_id)
    ser.write((str(payload) + "#").encode())
    if str(feed_id)== "bbc-led":
        global next_index
        next_index =1 -next_index
        client.publish("error_control","{ACK:" + str(next_index) +"}")


client = MQTTClient(AIO_USERNAME , AIO_KEY)
client.on_connect = connected
client.on_disconnect = disconnected
client.on_message = message
client.on_subscribe = subscribe
client.connect()
client.loop_background()

def getPort():
    ports = serial.tools.list_ports.comports()
    N = len(ports)
    commPort = "None"
    for i in range(0, N):
        port = ports[i]
        strPort = str(port)
        if "com0com" in strPort:  #USB Serial Device
            splitPort = strPort.split(" ")
            commPort = (splitPort[0])
    print(commPort)
    return commPort

ser = serial.Serial( port= getPort(), baudrate=115200)

mess = ""
def processData(data):
    data = data.replace("!", "")
    data = data.replace("#", "")
    splitData = data.split(":")
    print(splitData)
    if splitData[1] == "TEMP":
        client.publish("bbc-temp", splitData[2])
    if splitData[1] == "HUMID":
        client.publish("bbc-humid", splitData[2])
    

mess = ""
def readSerial():
    bytesToRead = ser.inWaiting()
    if (bytesToRead > 0):
        global mess
        mess = mess + ser.read(bytesToRead).decode("UTF-8")
        while ("#" in mess) and ("!" in mess):
            start = mess.find("!")
            end = mess.find("#")
            processData(mess[start:end + 1])
            if (end == len(mess)):
                mess = ""
            else:
                mess = mess[end+1:]
def data_pub():
    time.sleep(1.5)
    try:
        location = input("Enter the city name: ") 
        Weather=OpenWMap.weather_manager().weather_at_place(location) 
        Data=Weather.weather                  
        temp = Data.temperature(unit='celsius')     
        humidity = Data.humidity 
        client.publish("bbc-temp",temp['temp'])
        client.publish("bbc-humid",humidity)
    except NotFoundError:
        print("The city name is incorrect. Please enter again\n");

while True:
    readSerial()
    data_pub()
    time.sleep(1)