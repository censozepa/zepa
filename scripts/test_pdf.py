import requests
import re

js_url = 'https://natura2000.eea.europa.eu/Natura2000/static/js/main.5384b312.js'
js_resp = requests.get(js_url)

endpoints = re.findall(r'\"([^\"]*site[^\"]*)\"', js_resp.text)
for e in set(endpoints):
    if '/' in e:
        print(e)
        
endpoints2 = re.findall(r'\"([^\"]*sdf[^\"]*)\"', js_resp.text)
for e in set(endpoints2):
    if '/' in e:
        print(e)
