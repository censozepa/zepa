from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import time
from bs4 import BeautifulSoup

chrome_options = Options()
chrome_options.add_argument('--headless')
chrome_options.add_argument('--no-sandbox')
chrome_options.add_argument('--disable-dev-shm-usage')

try:
    driver = webdriver.Chrome(options=chrome_options)
    url = 'https://natura2000.eea.europa.eu/Natura2000/sdf/#/sdf?site=ES0000024'
    driver.get(url)
    print("Waiting for page to load...")
    time.sleep(10) # Blind wait since xpath failed
    
    html = driver.page_source
    soup = BeautifulSoup(html, 'html.parser')
    
    print("Finding tables...")
    tables = soup.find_all('table')
    print(f"Found {len(tables)} tables")
    
    for t in tables:
        headers = [th.text for th in t.find_all('th')]
        if any('Scientific Name' in str(h) for h in headers) or any('Scientific Name' in str(td) for td in t.find_all('td')):
            print("FOUND SPECIES TABLE")
            for tr in t.find_all('tr')[1:5]:
                tds = [td.text for td in tr.find_all('td')]
                print(" | ".join(tds[:5]))
    
    # Try finding PDF link
    links = soup.find_all('a', href=True)
    for l in links:
        if 'pdf' in l.text.lower() or 'pdf' in l['href'].lower() or 'download' in l['href'].lower():
            print("Download link:", l['href'])
            
    driver.quit()
except Exception as e:
    print(f"Error: {e}")
