#!/usr/bin/env python3
"""
Virtual Ulanzi TC001 Clock Simulator
Simulates Ulanzi-TC001-Evolved firmware v2.2.0 for testing with the Companion Android App in emulator.
Listens on 0.0.0.0:18080 (accessible from emulator at 10.0.2.2:18080 or host at 127.0.0.1:18080).
"""

import sys
import time
import json
import threading
from http.server import HTTPServer, BaseHTTPRequestHandler
from urllib.parse import urlparse, parse_qs

# Ensure UTF-8 output on Windows consoles
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")

PORT = 18080

SCREEN_NAMES = [
    "Clock",
    "Indoor Environment",
    "Outdoor Weather",
    "Crypto Ticker",
    "YouTube Analytics",
    "Live Airspace",
    "Battery & Power",
    "Stopwatch & Timer",
    "Ambient Screensaver"
]

class VirtualClockState:
    def __init__(self):
        self.lock = threading.Lock()
        self.reboot_until = 0.0

        # Configuration & Settings
        self.active_app = 0
        self.auto_rotate = True
        self.rotate_sec = 8
        self.carousel_mask = 0x1FF
        self.carousel_order = "0,1,2,3,4,5,6,7,8"
        self.transition_fx = 1
        self.battery_mode = 2  # 0: %, 1: V, 2: Alt

        # Clock & Date
        self.clk_mode = 1  # 0: Time, 1: Combined, 2: Date
        self.show_seconds = False
        self.weekday_bar = True
        self.weekday_start_mon = True
        self.weekday_active_col = 0
        self.weekday_inact_col = 0
        self.weekday_weekend_col = 0
        self.date_format = 0
        self.clock_icon = 0
        self.text_align = 0

        # Ambient & Brightness
        self.auto_bri = True
        self.day_bri = 60
        self.night_mode = True
        self.night_bri = 9
        self.night_start = 23
        self.night_end = 7

        # Services
        self.weather_city = "Bratislava"
        self.weather_lat = 48.1486
        self.weather_lon = 17.1077
        self.crypto_symbol = "BTC"
        self.yt_channel = "@CuriousCatFPV"
        self.yt_subscribers = "844"
        self.yt_has_key = True

        # MQTT
        self.mqtt_en = False
        self.mqtt_srv = "192.168.1.100"
        self.mqtt_port = 1883
        self.mqtt_user = ""
        self.mqtt_has_pass = False
        self.mqtt_pre = "ulanzi-tc001"
        self.mqtt_connected = False

        # Timer
        self.timer_mode = "countdown"
        self.timer_state = "stopped"
        self.timer_duration = 300
        self.timer_remaining = 300
        self.timer_elapsed_ms = 0
        self.timer_last_tick = time.time()

        # Screensaver
        self.screensaver_style = 0
        self.screensaver_timeout = 5
        self.screensaver_active = False

        # Custom App
        self.custom_apps = {}
        self.last_notification = None

        # Wi-Fi Profiles
        self.wifi_profiles = [
            {"slot": 1, "ssid": "HomeNetwork_5G", "has_pass": True, "active": True},
            {"slot": 2, "ssid": "Office_WiFi", "has_pass": True, "active": False},
            {"slot": 3, "ssid": "Pixel_Hotspot", "has_pass": True, "active": False},
            {"slot": 4, "ssid": "", "has_pass": False, "active": False}
        ]

    def trigger_reboot(self):
        with self.lock:
            print("\n🔄 [SIMULATOR] Remote hardware reboot triggered! Simulating restart for 2.5s...")
            self.reboot_until = time.time() + 2.5

    def is_rebooting(self):
        with self.lock:
            return time.time() < self.reboot_until

    def get_status_dict(self):
        with self.lock:
            now = time.localtime()
            sec_of_day = now.tm_hour * 3600 + now.tm_min * 60 + now.tm_sec
            app_idx = self.active_app % len(SCREEN_NAMES)

            return {
                "state": "connected",
                "ip": "10.0.2.2",
                "ap_ip": "192.168.4.1",
                "ssid": "HomeNetwork_5G",
                "rssi": -48,
                "rotate_sec": self.rotate_sec,
                "auto_rotate": self.auto_rotate,
                "carousel_mask": self.carousel_mask,
                "transition_fx": self.transition_fx,
                "active_app": app_idx,
                "app_name": SCREEN_NAMES[app_idx],
                "firmware_version": "2.3.0",
                "temperature_in": 23.4,
                "humidity_in": 48.2,
                "battery_volts": 4.15,
                "battery_pct": 95,
                "ldr_raw": 1920,
                "auto_bri": self.auto_bri,
                "day_bri": self.day_bri,
                "night_bri": self.night_bri,
                "night_mode": self.night_mode,
                "weather_temp": "19°C",
                "weather_city": self.weather_city,
                "crypto_symbol": self.crypto_symbol,
                "crypto_price": "67.8K",
                "yt_subscribers": self.yt_subscribers,
                "timer_mode": self.timer_mode,
                "timer_state": self.timer_state,
                "timer_duration": self.timer_duration,
                "timer_remaining": self.timer_remaining,
                "timer_elapsed_ms": self.timer_elapsed_ms,
                "screensaver_style": self.screensaver_style,
                "screensaver_timeout": self.screensaver_timeout,
                "screensaver_active": self.screensaver_active,
                "car_ord": self.carousel_order,
                "bat_mode": self.battery_mode,
                "clk_mode": self.clk_mode,
                "show_sec": self.show_seconds,
                "wd_bar": self.weekday_bar,
                "wd_mon": self.weekday_start_mon,
                "wd_acol": self.weekday_active_col,
                "wd_icol": self.weekday_inact_col,
                "wd_wcol": self.weekday_weekend_col,
                "date_fmt": self.date_format,
                "clk_icon": self.clock_icon,
                "txt_align": self.text_align,
                "night_start": self.night_start,
                "night_end": self.night_end,
                "weather_lat": self.weather_lat,
                "weather_lon": self.weather_lon,
                "yt_channel": self.yt_channel,
                "yt_has_key": self.yt_has_key,
                "mqtt_en": self.mqtt_en,
                "mqtt_srv": self.mqtt_srv,
                "mqtt_port": self.mqtt_port,
                "mqtt_user": self.mqtt_user,
                "mqtt_has_pass": self.mqtt_has_pass,
                "mqtt_pre": self.mqtt_pre,
                "ui_lang": "en",
                "mqtt_connected": self.mqtt_connected,
                "wifi_profiles": self.wifi_profiles
            }

    def generate_display_pixels(self):
        with self.lock:
            # 8 rows x 32 columns
            pixels = [["#0B0B0A" for _ in range(32)] for _ in range(8)]
            app = self.active_app % 9

            # Color palette per app
            app_colors = [
                "#00F0FF", # 0: Clock
                "#FF4F00", # 1: Indoor Temp SHT30
                "#00D062", # 2: Weather
                "#FFD700", # 3: Crypto
                "#FF0033", # 4: YouTube
                "#00E5FF", # 5: Airspace Radar
                "#76FF03", # 6: Battery
                "#E5A93C", # 7: Timer
                "#9C27B0"  # 8: Screensaver
            ]
            col = app_colors[app]

            # Render 4x5 icon block at columns 1..5, rows 1..5
            for y in range(1, 6):
                for x in range(1, 6):
                    if (x == 1 or x == 5 or y == 1 or y == 5 or (x == 3 and y == 3)):
                        pixels[y][x] = col

            # Render text/bars block at columns 8..30, rows 2..5
            for y in range(2, 6):
                for x in range(8, 28):
                    if (x + y) % 3 == 0:
                        pixels[y][x] = col

            # Row 7: Weekday indicator bars (7 segments if enabled)
            if self.weekday_bar:
                today_idx = time.localtime().tm_wday  # 0=Monday
                for d in range(7):
                    bar_col = col if d == today_idx else "#252525"
                    bx = 9 + d * 3
                    if bx < 32:
                        pixels[7][bx] = bar_col
                        if bx + 1 < 32:
                            pixels[7][bx + 1] = bar_col

            return pixels

clock_state = VirtualClockState()

class UlanziSimulatorHandler(BaseHTTPRequestHandler):
    def log_message(self, format, *args):
        # Concise one-line terminal request logger
        sys.stdout.write(f"[{time.strftime('%H:%M:%S')}] {self.command} {self.path} -> {args[1]}\n")
        sys.stdout.flush()

    def send_cors_headers(self):
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type, X-Requested-With")

    def do_OPTIONS(self):
        self.send_response(200)
        self.send_cors_headers()
        self.end_headers()

    def do_GET(self):
        if clock_state.is_rebooting():
            self.send_response(503)
            self.end_headers()
            self.wfile.write(b"Device rebooting...")
            return

        parsed = urlparse(self.path)
        path = parsed.path
        qs = parse_qs(parsed.query)

        if path == "/" or path == "/index.html":
            self.send_response(200)
            self.send_header("Content-Type", "text/html")
            self.send_cors_headers()
            self.end_headers()
            html = f"""<!DOCTYPE html>
<html><head><title>Virtual Ulanzi TC001 Clock Simulator</title></head>
<body style="background:#121211;color:#F4F3EF;font-family:monospace;padding:20px;">
<h2>ULANZI TC001 VIRTUAL SIMULATOR (FW v2.2.0)</h2>
<p>Active Screen: <b>{SCREEN_NAMES[clock_state.active_app % 9]}</b> (ID {clock_state.active_app})</p>
<p>Weekday Bars: <b>{'ON' if clock_state.weekday_bar else 'OFF'}</b></p>
<p>Port: 18080 &bull; Ready for Android Companion App</p>
</body></html>"""
            self.wfile.write(html.encode("utf-8"))
            return

        elif path == "/status":
            st = clock_state.get_status_dict()
            body = json.dumps(st).encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_cors_headers()
            self.end_headers()
            self.wfile.write(body)
            return

        elif path == "/api/display":
            px = clock_state.generate_display_pixels()
            body = json.dumps({"pixels": px}).encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_cors_headers()
            self.end_headers()
            self.wfile.write(body)
            return

        elif path == "/api/screen":
            with clock_state.lock:
                if "auto" in qs:
                    clock_state.auto_rotate = (qs["auto"][0] == "1")
                if "set" in qs:
                    try:
                        clock_state.active_app = int(qs["set"][0]) % 9
                    except ValueError:
                        pass
                app_idx = clock_state.active_app
                name = SCREEN_NAMES[app_idx]
                auto = clock_state.auto_rotate
                print(f"🖥️ [SCREEN SWITCH] Set active screen -> [{app_idx}] {name} (auto_rotate: {auto})")

            resp = {"active_app": app_idx, "auto_rotate": auto, "app_name": name}
            body = json.dumps(resp).encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_cors_headers()
            self.end_headers()
            self.wfile.write(body)
            return

        elif path == "/api/timer":
            with clock_state.lock:
                act = qs.get("action", [""])[0].lower()
                mode = qs.get("mode", [""])[0].lower()
                dur = qs.get("duration", [""])[0]

                if mode:
                    clock_state.timer_mode = "stopwatch" if mode in ["0", "stopwatch"] else "countdown"
                if dur.isdigit():
                    clock_state.timer_duration = int(dur)
                    clock_state.timer_remaining = int(dur)

                if act == "start":
                    clock_state.timer_state = "running"
                elif act == "pause":
                    clock_state.timer_state = "paused"
                elif act == "toggle":
                    clock_state.timer_state = "paused" if clock_state.timer_state == "running" else "running"
                elif act == "reset":
                    clock_state.timer_state = "stopped"
                    clock_state.timer_remaining = clock_state.timer_duration
                    clock_state.timer_elapsed_ms = 0

                resp = {
                    "mode": clock_state.timer_mode,
                    "state": clock_state.timer_state,
                    "duration": clock_state.timer_duration,
                    "remaining": clock_state.timer_remaining,
                    "elapsed_ms": clock_state.timer_elapsed_ms
                }
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_cors_headers()
            self.end_headers()
            self.wfile.write(json.dumps(resp).encode("utf-8"))
            return

        elif path == "/api/screensaver":
            with clock_state.lock:
                st = qs.get("style", [""])[0]
                to = qs.get("timeout", [""])[0]
                act = qs.get("action", [""])[0].lower()
                if st.isdigit(): clock_state.screensaver_style = int(st)
                if to.isdigit(): clock_state.screensaver_timeout = int(to)
                if act in ["start", "activate"]: clock_state.screensaver_active = True
                elif act in ["stop", "deactivate"]: clock_state.screensaver_active = False

                resp = {
                    "style": clock_state.screensaver_style,
                    "active_style": clock_state.screensaver_style,
                    "timeout": clock_state.screensaver_timeout,
                    "active": clock_state.screensaver_active,
                    "idle_ms": 0
                }
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_cors_headers()
            self.end_headers()
            self.wfile.write(json.dumps(resp).encode("utf-8"))
            return

        elif path == "/api/wifi":
            with clock_state.lock:
                resp = {
                    "connected": True,
                    "active_ssid": "HomeNetwork_5G",
                    "ip": "10.0.2.2",
                    "rssi": -48,
                    "profiles": clock_state.wifi_profiles
                }
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_cors_headers()
            self.end_headers()
            self.wfile.write(json.dumps(resp).encode("utf-8"))
            return

        elif path == "/scan":
            nets = [
                {"ssid": "HomeNetwork_5G", "rssi": -45, "secure": True},
                {"ssid": "Office_WiFi", "rssi": -62, "secure": True},
                {"ssid": "Pixel_Hotspot", "rssi": -55, "secure": True},
                {"ssid": "Guest_Open", "rssi": -78, "secure": False}
            ]
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_cors_headers()
            self.end_headers()
            self.wfile.write(json.dumps(nets).encode("utf-8"))
            return

        elif path in ["/api/reboot", "/reboot"]:
            clock_state.trigger_reboot()
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_cors_headers()
            self.end_headers()
            self.wfile.write(b'{"status":"rebooting"}')
            return

        else:
            self.send_response(404)
            self.end_headers()
            self.wfile.write(b'{"error":"Not Found"}')

    def do_POST(self):
        if clock_state.is_rebooting():
            self.send_response(503)
            self.end_headers()
            self.wfile.write(b"Device rebooting...")
            return

        parsed = urlparse(self.path)
        path = parsed.path
        length = int(self.headers.get("Content-Length", 0))
        raw_body = self.rfile.read(length).decode("utf-8", errors="ignore") if length > 0 else ""

        if path in ["/api/reboot", "/reboot"]:
            clock_state.trigger_reboot()
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_cors_headers()
            self.end_headers()
            self.wfile.write(b'{"status":"rebooting"}')
            return

        elif path == "/save":
            form = parse_qs(raw_body)
            with clock_state.lock:
                print(f"💾 [SAVE] Received settings save parameters: {list(form.keys())}")
                if "clk_mode" in form: clock_state.clk_mode = int(form["clk_mode"][0])
                if "date_fmt" in form: clock_state.date_format = int(form["date_fmt"][0])
                if "clk_icon" in form: clock_state.clock_icon = int(form["clk_icon"][0])
                if "txt_align" in form: clock_state.text_align = int(form["txt_align"][0])
                clock_state.show_seconds = ("show_sec" in form)
                clock_state.weekday_bar = ("wd_bar" in form)
                print(f"   -> Weekday Bars set to: {clock_state.weekday_bar}")
                if "wd_mon" in form: clock_state.weekday_start_mon = (form["wd_mon"][0] == "1")
                if "wd_acol" in form: clock_state.weekday_active_col = int(form["wd_acol"][0])
                if "wd_icol" in form: clock_state.weekday_inact_col = int(form["wd_icol"][0])
                if "wd_wcol" in form: clock_state.weekday_weekend_col = int(form["wd_wcol"][0])

                clock_state.auto_bri = ("auto_bri" in form)
                if "day_bri" in form: clock_state.day_bri = int(form["day_bri"][0])
                clock_state.night_mode = ("night_mode" in form)
                if "night_bri" in form: clock_state.night_bri = int(form["night_bri"][0])
                if "night_start" in form: clock_state.night_start = int(form["night_start"][0])
                if "night_end" in form: clock_state.night_end = int(form["night_end"][0])

                clock_state.auto_rotate = ("auto_rotate" in form)
                if "rotate" in form: clock_state.rotate_sec = int(form["rotate"][0])
                if "trans_fx" in form: clock_state.transition_fx = int(form["trans_fx"][0])
                if "active_app" in form: clock_state.active_app = int(form["active_app"][0]) % 9
                if "car_ord" in form: clock_state.carousel_order = form["car_ord"][0]

                if "bat_mode" in form: clock_state.battery_mode = int(form["bat_mode"][0])
                if "ss_style" in form: clock_state.screensaver_style = int(form["ss_style"][0])
                if "ss_time" in form: clock_state.screensaver_timeout = int(form["ss_time"][0])

                if "w_city" in form: clock_state.weather_city = form["w_city"][0]
                if "w_lat" in form: clock_state.weather_lat = float(form["w_lat"][0])
                if "w_lon" in form: clock_state.weather_lon = float(form["w_lon"][0])
                if "c_sym_custom" in form and form["c_sym_custom"][0]:
                    clock_state.crypto_symbol = form["c_sym_custom"][0]
                elif "c_sym" in form:
                    clock_state.crypto_symbol = form["c_sym"][0]

                if "yt_channel" in form:
                    clock_state.yt_channel = form["yt_channel"][0]
                    if "curiouscat" in clock_state.yt_channel.lower() or "ucbfgwmm6pccdsxyoaoomgmq" in clock_state.yt_channel.lower():
                        clock_state.yt_subscribers = "844"
                clock_state.mqtt_en = ("mqtt_en" in form)
                if "mqtt_srv" in form: clock_state.mqtt_srv = form["mqtt_srv"][0]
                if "mqtt_port" in form: clock_state.mqtt_port = int(form["mqtt_port"][0])
                if "mqtt_user" in form: clock_state.mqtt_user = form["mqtt_user"][0]
                if "mqtt_pre" in form: clock_state.mqtt_pre = form["mqtt_pre"][0]

            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_cors_headers()
            self.end_headers()
            self.wfile.write(b'{"status":"success"}')
            return

        elif path == "/api/notify":
            try:
                data = json.loads(raw_body)
            except Exception:
                data = parse_qs(raw_body)
            print(f"🔔 [NOTIFICATION RECEIVED]: {data}")
            clock_state.last_notification = data
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_cors_headers()
            self.end_headers()
            self.wfile.write(b'{"status":"notified"}')
            return

        elif path == "/api/custom":
            qs = parse_qs(parsed.query)
            app_name = qs.get("name", ["spotify"])[0]
            try:
                data = json.loads(raw_body)
            except Exception:
                data = {"text": raw_body}
            print(f"🎵 [CUSTOM APP PUSH] [{app_name}]: {data}")
            clock_state.custom_apps[app_name] = data
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_cors_headers()
            self.end_headers()
            self.wfile.write(json.dumps({"status": "ok", "app": app_name}).encode("utf-8"))
            return

        elif path == "/api/timer":
            # Delegate to GET handler which handles query params
            return self.do_GET()

        elif path == "/api/screensaver":
            return self.do_GET()

        else:
            self.send_response(404)
            self.end_headers()
            self.wfile.write(b'{"error":"Not Found"}')

    def do_DELETE(self):
        parsed = urlparse(self.path)
        path = parsed.path
        if path == "/api/custom":
            qs = parse_qs(parsed.query)
            app_name = qs.get("name", [""])[0]
            if app_name:
                clock_state.custom_apps.pop(app_name, None)
                print(f"🗑️ [CUSTOM APP DELETE] Cleared '{app_name}'")
            else:
                clock_state.custom_apps.clear()
                print("🗑️ [CUSTOM APP CLEAR] Cleared all custom apps")
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_cors_headers()
            self.end_headers()
            self.wfile.write(b'{"status":"deleted"}')
            return
        self.send_response(404)
        self.end_headers()

def run_server():
    server = HTTPServer(("0.0.0.0", PORT), UlanziSimulatorHandler)
    print("=================================================================")
    print(f"🚀 Ulanzi TC001 Virtual Clock Simulator running on port {PORT}!")
    print(f"   - Host connection URL:     http://127.0.0.1:{PORT}")
    print(f"   - Emulator connection URL: http://10.0.2.2:{PORT}")
    print(f"   - Firmware Emulation:      v2.2.0 (Ulanzi-TC001-Evolved)")
    print("=================================================================")
    sys.stdout.flush()
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nStopping simulator...")
        server.server_close()

if __name__ == "__main__":
    run_server()
