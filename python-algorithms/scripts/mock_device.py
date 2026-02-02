
import requests
import time
import random
import argparse
import datetime
import json

# Configuration
API_URL = "http://localhost:8098/health/mock/upload"

def generate_data(device_id, scenario="normal"):
    """
    Generate a single data point based on the scenario.
    """
    timestamp = int(time.time() * 1000)
    
    if scenario == "normal":
        heart_rate = random.randint(60, 100)
        sys_bp = random.randint(110, 130)
        dia_bp = random.randint(70, 85)
    elif scenario == "abnormal_high":
        heart_rate = random.randint(110, 140) # High Heart Rate
        sys_bp = random.randint(140, 160)     # High BP
        dia_bp = random.randint(90, 100)
    
    # Format: "sys/dia"
    bp_str = f"{sys_bp}/{dia_bp}"
    
    # 5% chance of missing data or outliers in normal mode for realism, 
    # but let's keep it simple for now to ensure stable demo.
    
    data_point = {
        "heart_rate": heart_rate,
        "blood_pressure": bp_str,
        "steps": random.randint(0, 10), # Incremental steps not tracked here, just instantaneous
        "timestamp": timestamp
    }
    
    return data_point

def send_data(device_id, data_list, user_id=None):
    """
    Send data to the backend.
    """
    payload = {
        "deviceId": device_id,
        "dataList": data_list
    }
    
    if user_id:
        payload["userId"] = user_id
        
    try:
        headers = {'Content-Type': 'application/json'}
        response = requests.post(API_URL, data=json.dumps(payload), headers=headers)
        
        if response.status_code == 200:
            print(f"[{datetime.datetime.now()}] Sent {len(data_list)} points - Status: {response.json().get('msg')}")
            # Print response data if interesting
            if 'data' in response.json():
                 print(f"   -> Server Analysis: {json.dumps(response.json()['data'], ensure_ascii=False)}")
        else:
            print(f"[{datetime.datetime.now()}] Failed: {response.status_code} - {response.text}")
            
    except Exception as e:
        print(f"[{datetime.datetime.now()}] Error: {e}")

def main():
    parser = argparse.ArgumentParser(description='Mock Device Data Generator')
    parser.add_argument('--device_id', type=str, default='WATCH_MOCK_001', help='Device ID')
    parser.add_argument('--user_id', type=int, default=1, help='User ID (optional)')
    parser.add_argument('--scenario', type=str, default='normal', choices=['normal', 'abnormal_high'], help='Data Scenario')
    parser.add_argument('--interval', type=float, default=2.0, help='Interval in seconds')
    
    args = parser.parse_args()
    
    print(f"Starting Mock Device: {args.device_id}")
    print(f"Scenario: {args.scenario}")
    print(f"Target: {API_URL}")
    print("Press Ctrl+C to stop.")
    
    try:
        while True:
            # Generate 1 data point
            data = generate_data(args.device_id, args.scenario)
            
            # Send it in a list (batch of 1)
            send_data(args.device_id, [data], args.user_id)
            
            time.sleep(args.interval)
            
    except KeyboardInterrupt:
        print("\nStopped.")

if __name__ == "__main__":
    main()
