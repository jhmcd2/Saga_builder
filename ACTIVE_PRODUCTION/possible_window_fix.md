You are running directly into Chrome’s security model regarding the Fullscreen API, which explicitly blocks programmatic fullscreen requests unless they are triggered by a transient user gesture (like an actual mouse click or key press). Chrome does this to prevent malicious sites from hijacking a user's monitor. [1, 2] 
Since your assistance device controller needs to automate this process, there are a few robust workarounds depending on how much control you have over the client machine.
## Workaround 1: Emulate the User Gesture via a Chrome Extension (Best for Automation)
If you are developing a companion Chrome Extension for your assistance device, you can use Chrome's internal debugging tools to fake a native hardware event. Regular JavaScript .click() events will fail because Chrome tracks the activation origin, but the chrome.debugger API can bypass this restriction. [3, 4] 

   1. Add "debugger" to your extension's manifest.json permissions.
   2. Use the debugger to attach to the target tab and dispatch a trusted mouse event: [3] 

// Attach to the streaming tab
chrome.debugger.attach({ tabId: targetTabId }, "1.3", () => {
  // Send a trusted mouse down/up sequence to fake a physical user gesture
  chrome.debugger.sendCommand({ tabId: targetTabId }, "Input.dispatchMouseEvent", {
    type: "mousePressed",
    x: 100,
    y: 100,
    button: "left",
    clickCount: 1
  }, () => {
    chrome.debugger.sendCommand({ tabId: targetTabId }, "Input.dispatchMouseEvent", {
      type: "mouseReleased",
      x: 100,
      y: 100,
      button: "left",
      clickCount: 1
    }, () => {
      // Detach when done
      chrome.debugger.detach({ tabId: targetTabId });
    });
  });
});


   1. Once that trusted event fires, your background script or content script can immediately trigger document.querySelector('video').requestFullscreen().

## Workaround 2: Use Chrome Launch Flags (No Extension Needed)
If you can control how Chrome is launched on the device (e.g., via a startup script, command line, or application shortcut), you can disable the gesture requirement entirely using Chrome flags. [5, 6] 
Launch Chrome with the following arguments: [5] 

chrome.exe --autoplay-policy=no-user-gesture-required --disable-gesture-requirement-for-presentation


* 
* --autoplay-policy=no-user-gesture-required removes restrictions around background media state initialization.
* --disable-gesture-requirement-for-presentation forces Chrome to waive the gesture restriction for web presentation layers. [5] 
* 

## Workaround 3: Native OS Keystroke Injection
If your assistance controller operates at the Operating System level (like a background Python, C#, or Node script), the absolute cleanest method is to completely bypass the web APIs and inject the global Fullscreen Hotkey directly into the active window.

* 
* For the Browser window: Send an F11 keystroke (Windows/Linux) or Cmd + Ctrl + F (macOS) to force Chrome to fill the screen.
* For the Pluto Player specifically: Pluto TV’s web player typically toggles video-only fullscreen when it receives the f key while the player is in focus. Sending a native OS click to focus the middle of the window, followed by a simulated f keystroke, usually overrides the native browser block because the browser views it as a real physical keyboard event. [4, 7, 8, 9] 
* 

## Workaround 4: The "Automatic Fullscreen" Enterprise Policy
Chrome includes an enterprise administrative override specifically designed for kiosk and assistive setups called AutomaticFullscreenAllowedForUrls. [10, 11] 
If you set this policy on the machine's registry (or via Chrome Management), you can allow Pluto's URL (https://pluto.tv) to request fullscreen with zero user interaction. Once allowed, you can just call the standard .requestFullscreen() JavaScript method seamlessly. [10, 11] 
To help narrow down the best solution, let me know:

* 
* What programming language or framework is your controller written in?
* Do you have the ability to install extensions or modify browser flags on the target computer?
* 


[1] [https://forum.playcanvas.com](https://forum.playcanvas.com/t/browser-being-forced-full-screen-without-user-input/24972)
[2] [https://devtoolstips.org](https://devtoolstips.org/tips/en/emulate-user-gesture-in-console/)
[3] [https://bugs.chromium.org](https://bugs.chromium.org/p/chromium/issues/detail?id=666824)
[4] [https://www.reddit.com](https://www.reddit.com/r/chrome/comments/1bksz24/is_there_a_way_to_trigger_fullscreen_without_user/)
[5] [https://stackoverflow.com](https://stackoverflow.com/questions/71696924/google-chrome-is-there-any-flag-to-disable-the-need-of-user-interact-to-fullscr)
[6] [https://stackoverflow.com](https://stackoverflow.com/questions/30018320/fullscreen-chrome-without-user-action)
[7] [https://www.youtube.com](https://www.youtube.com/watch?v=77pEr9V9j5w&t=7)
[8] [https://www.reddit.com](https://www.reddit.com/r/techtalktutorials/comments/1jl8j2c/how_to_turn_on_or_off_full_screen_mode_in_the/)
[9] [https://www.reddit.com](https://www.reddit.com/r/chrome/comments/1bksz24/is_there_a_way_to_trigger_fullscreen_without_user/)
[10] [https://chromestatus.com](https://chromestatus.com/feature/6218822004768768)
[11] [https://github.com](https://github.com/explainers-by-googlers/html-fullscreen-without-a-gesture/blob/main/HOWTO.md)

Because you are already connecting to Chrome via WebSockets, you are likely utilizing the Chrome DevTools Protocol (CDP). This is actually your biggest advantage. The reason Pluto chokes during a "roll over" or "next video" event is because Pluto's React app destroys the old video element/player layer and creates a brand-new one in the DOM. When this happens, any previous fullscreen states or DOM bindings are lost, and Chrome flags the new media element as requiring a fresh user gesture.
Since you already have a Python WebSocket connection and specific buttons to target the tab, you can bypass standard web API restrictions completely.
## Solution 1: Use CDP to Inject a Trusted Gesture (Most Robust)
Since standard JavaScript element.requestFullscreen() fails without a gesture, you can use your Python WebSocket connection to issue a trusted hardware event directly to the page via CDP's Input.dispatchMouseEvent. This fools Chrome into thinking a human clicked the screen, which satisfies the security check right as the new video loads.
Using your Python WebSocket client, send the following CDP payload sequence to the targeted tab right when the video changes:

import jsonimport time
# Target coordinates where the Pluto player sits (e.g., center of screen)x, y = 500, 500 
# 1. Simulate Mouse Pressawait websocket.send(json.dumps({
    "id": 1,
    "method": "Input.dispatchMouseEvent",
    "params": {
        "type": "mousePressed",
        "x": x,
        "y": y,
        "button": "left",
        "clickCount": 1
    }
}))
# Small delay to mimic a real click duration
time.sleep(0.05)
# 2. Simulate Mouse Release (This unlocks the "User Gesture" token in Chrome)await websocket.send(json.dumps({
    "id": 2,
    "method": "Input.dispatchMouseEvent",
    "params": {
        "type": "mouseReleased",
        "x": x,
        "y": y,
        "button": "left",
        "clickCount": 1
    }
}))
# 3. Immediately evaluate the Fullscreen command while the gesture token is activeawait websocket.send(json.dumps({
    "id": 3,
    "method": "Runtime.evaluate",
    "params": {
        "expression": "document.querySelector('video').requestFullscreen();"
    }
}))

## Solution 2: Automated Interception of Pluto's Video Swaps
To handle the "Next Video" rollover automatically without making the user click your floating button again, you can inject a small JavaScript MutationObserver into the tab via your WebSocket connection. This script will watch Pluto's DOM, detect when the player is replaced, and automatically send a signal back or trigger the fullscreen API instantly.
You can execute this script via your Python WebSocket once the tab is armed:

// JS to evaluate in the tab via CDP Runtime.evaluateconst observer = new MutationObserver((mutations) => {
    const video = document.querySelector('video');
    if (video && !document.fullscreenElement) {
        // A new video element was created by Pluto's player swap
        console.log("New video detected, attempting fullscreen stabilization...");
        
        // If your Python code is listening to console logs via CDP Runtime.consoleAPICalled,
        # you can use this log to trigger the Python script to send the CDP mouse click above.
    }
});

observer.observe(document.body, { childList: true, subtree: true });

## Solution 3: The Native OS Keypress Fallback (Since you are in Python)
If Pluto's DOM structure makes targeting the video element via CDP unreliable during transitions, you can leverage your Python environment to control the browser from the OS level. When your floating button triggers the routine:

   1. Use Python to issue an OS-level window focus to Chrome.
   2. Use a library like pyautogui or pynput to press F11 (forces Chrome into full screen window mode).
   3. Immediately send a physical f key press via Python. Pluto’s web player natively captures the f key to toggle the player itself into full screen, and because it is an OS-level simulation, Chrome cannot block it via its web sandbox protections.

To refine the code, let me know:

* Which specific Python library (like websockets, playwright, or selenium) are you using to handle the WebSocket connection?
* When it chokes during the rollover, does Chrome throw a specific console error (like Permissions check failed), or does the video simply freeze/drop frames?


