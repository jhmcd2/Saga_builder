To stop your AMD GPU from triggering your PSU's safety circuitry during rapid audio generation loops, you need to lock the card into a constant, flat clock state and enforce a hard power limit using AMD's native tooling.
Since AMD does not use nvidia-smi, you must manage transient power spikes (micro-surges) using rocm-smi (on Linux) or the AMD Radeon Software CLI / Registry tools (on Windows). [1] 
## How to Lock GPU Performance and Cap Wattage

* 
* Lock the Performance Level: Set the GPU performance level to high or manual rather than auto. This keeps the voltage and clocks steady between your 5-prompt batch cycles, eliminating the aggressive idle-to-peak power spikes that trip your PSU.
* Cap the Power Limit Explicitly: Use the Management Interface to drop the total board power (TBP) ceiling by a specific wattage or percentage, ensuring even peak micro-surges stay below your PSU's tripwire.
* Linux Command Line (rocm-smi):
* Lock power state: rocm-smi --setperflevel high
   * Enforce a power cap: rocm-smi --setpoweroverdrive <wattage> (e.g., lower it by 50-100W below stock).
* Windows Automation: If you are automating via Python on Windows, use command-line injection via tools like OverdriveNTool or call AMD's libraries directly to lock your clock states and lower the "Power Limit %" slider permanently for the session.
* 

## Python Implementation for Automation
You can integrate these hardware locks directly into your Python dataset script. Run this setup block before your ComfyUI generation queue starts:

import subprocessimport time
def lock_amd_gpu_power():
    try:
        # 1. Force the GPU to maintain a constant performance level (prevents idle-to-peak spikes)
        subprocess.run(["rocm-smi", "--setperflevel", "high"], check=True)
        
        # 2. Hard cap the power limit (adjust the wattage number to find your PSU's stable sweet spot)
        # Replace 300 with a value safely below your card's maximum rated board power
        subprocess.run(["rocm-smi", "--setpoweroverdrive", "300"], check=True)
        
        print("GPU locked to high performance state with strict power limits.")
    except Exception as e:
        print(f"Could not set AMD power limits via CLI: {e}")
# Call this before starting your dataset loop
lock_amd_gpu_power()

## Why This Is Happening on Your 1050W PSU
Even though 1050W sounds like plenty, high-end AMD cards can exhibit massive transient power spikes lasting only a few milliseconds. When ComfyUI finishes a batch of 5 audio bites, the GPU drops to near-idle power instantly. The moment the next 5 bites queue up, the GPU demands maximum current in less than a millisecond.
An 80 Plus Bronze efficiency rating generally indicates older internal component architecture. These older platforms have slower analog safety circuits (Over-Current Protection / Over-Power Protection) that mistake a massive, ultra-fast millisecond current delta for a short circuit, causing the PSU to instantly shut down to save itself. Keeping the performance state locked to high ensures the power floor never drops, smoothing out that fatal delta curve.
If you'd like to implement this on Windows instead of Linux, let me know your exact operating system and AMD GPU model number so I can provide the precise CLI tools or registry hacks needed to lock the clocks. [2] 

[1] [https://www.reddit.com](https://www.reddit.com/r/linuxquestions/comments/af8sdl/something_as_sophisticated_as_nvidiasmi_or_nvtop/)
[2] [https://www.reddit.com](https://www.reddit.com/r/hackintosh/comments/qtigkn/help_using_both_amd_and_nvidia_gpu_in_the_same_pc/)
Since you are running Windows 10, you have two practical paths forward. You can fix this directly on Windows using MSI Afterburner, or you can containerize using Docker with WSL2 to access native Linux ROCm power-management tools. [1] 
Because official AMD SMI command-line tools remain largely Linux-centric, containerizing is the best choice if you want strict automated Python integration. [2] 
------------------------------
## Option 1: The Native Windows 10 Solution (Easiest)
Windows 10 does not have a reliable first-party CLI tool like nvidia-smi to lock clocks via Python. The absolute best way to manage this on Windows is using MSI Afterburner to build a flat power profile that prevents power deltas. [2] 

   1. Flatten the Curve (Underclock & Undervolt): Open the Voltage/Frequency Curve Editor in MSI Afterburner. Instead of letting the card aggressively spike up and down, flatten the line at a lower voltage/frequency threshold. This keeps the voltage constant whether ComfyUI is generating or idling.
   2. Cap Power Limit: Lower the Power Limit (%) slider to roughly 80% to 85%. This limits the total wattage peak during high-intensity inference. [3] 
   3. Set Power Management Mode: In Windows 10, go to Control Panel > Power Options and ensure your profile is set to High Performance. This keeps the PCI-Express bus and CPU power delivery awake, minimizing systemic power drops between your 5-prompt batch cycles. [3, 4] 

------------------------------
## Option 2: The Containerized Solution (Best for Automation)
Containerizing allows your Python script to communicate directly with the GPU hardware states via WSL2 (Windows Subsystem for Linux). This unlocks the exact rocm-smi behavior discussed earlier. [5, 6] 
## Step 1: Install WSL2 & AMD Drivers [7] 
Ensure you have the latest [AMD Radeon Adrenalin Drivers](https://transformerlab.ai/docs/install/install-on-amd) installed on Windows 10. WSL2 passes the GPU through natively using the host driver. Open PowerShell as Administrator and run: [8] 

wsl --install

## Step 2: Set Up Docker with WSL2 Backend [9] 
Install Docker Desktop for Windows and ensure "Use the WSL 2 based engine" is checked in Settings > General. [10, 11, 12] 
## Step 3: Use the ROCm Base Image
To run ComfyUI and use rocm-smi inside a container, use an official AMD ROCm base image. Create a Dockerfile: [13] 

# Use the official AMD ROCm PyTorch image as a baseFROM rocm/pytorch:rocm6.0_ubuntu22.04
# Install basic dependencies and toolsRUN apt-get update && apt-get install -y \
    python3-pip \
    git \
    kmod \
    && rm -rf /var/lib/apt/lists/*
# Set up ComfyUIWORKDIR /appRUN git clone https://github.com .RUN pip3 install --upgrade pipRUN pip3 install -r requirements.txt
# Expose ComfyUI's default portEXPOSE 8188
# Command to run ComfyUICMD ["python3", "main.py", "--listen", "0.0.0.0"]

## Step 4: Run the Container with Hardware Access [14] 
When launching your container, you must pass the DRI hardware video devices (/dev/dri) from Windows into the container so rocm-smi can execute power commands:

docker run -d --name comfyui-amd `
  --device=/dev/kfd --device=/dev/dri `
  --group-add video `
  -p 8188:8188 `
  -v C:\your_models_path:/app/models `
  comfyui-rocm-image

Once inside this container, your automated Python script will be able to successfully make subprocess calls to rocm-smi to lock your GPU states, capping the wattage to neutralize the micro-surges pulling down your 80 Bronze PSU. [3, 15] 
------------------------------
If you choose the Docker route, let me know if you run into any WSL2 hardware mounting errors or if you need help passing your local dataset directories into the container!

[1] [https://medium.com](https://medium.com/@tawrinkle/how-i-prep-a-windows-10-11-machine-for-devops-work-without-fighting-it-later-8e3636449678)
[2] [https://github.com](https://github.com/ROCm/rocm-systems/discussions/8553)
[3] [https://www.reddit.com](https://www.reddit.com/r/comfyui/comments/1q5zp3d/is_it_possible_to_just_throttle_gpu_usage_in/)
[4] [https://sahebg.github.io](https://sahebg.github.io/gaming/dota2-windows-10-crash-freeze-issues/)
[5] [https://upsidedwn.medium.com](https://upsidedwn.medium.com/streamline-your-development-workflow-with-wsl2-d242ac5f9699)
[6] [https://vishnubraj.medium.com](https://vishnubraj.medium.com/beginners-guide-to-using-singularity-and-slurm-for-high-performance-computing-hpc-896464a8b897)
[7] [https://geoligard.com](https://geoligard.com/docker-on-wsl-with-phpstorm-best-of-both-worlds)
[8] [https://transformerlab.ai](https://transformerlab.ai/docs/install/install-on-amd)
[9] [https://community.toradex.com](https://community.toradex.com/t/update-and-fresh-install-of-torizon-ide-extension-failures/26729)
[10] [https://www.theodo.com](https://www.theodo.com/blog/install-and-run-docker-natively-on-windows-10-home)
[11] [https://reprodev.com](https://reprodev.com/windows-docker-desktop-wsl2/)
[12] [https://chloesun.medium.com](https://chloesun.medium.com/set-up-linux-dev-environment-on-windows-machine-with-wsl2-docker-and-vs-code-25f167aa71ab)
[13] [https://ekanayakasalitha.medium.com](https://ekanayakasalitha.medium.com/deploying-a-spring-boot-application-using-docker-in-linux-environment-92ff4f4cf296)
[14] [https://www.reddit.com](https://www.reddit.com/r/docker/comments/1jpo3gi/protecting_code_in_a_docker_container/)
[15] [https://www.reddit.com](https://www.reddit.com/r/comfyui/comments/1re8iu7/gpu_is_causing_breaker_to_trip_while_generating/)
If you are running your script within a Python virtual environment (.venv) directly on native Windows 10, the provided Python code using rocm-smi will not work.
Because virtual environments share the host operating system's kernel, a Windows .venv can only execute standard Windows applications and libraries. It cannot run Linux binary tools like rocm-smi or amdsmi because the low-level AMD drivers for Windows do not expose those command-line interfaces. [1, 2] 
To execute automated power-capping scripts, choose one of the following approaches based on your preferred configuration:
------------------------------
## Path A: Run inside a WSL2 Container (Recommended for Automation)
If you containerize using Docker with the WSL2 backend (as detailed in the previous step), the container handles the heavy lifting. Because WSL2 runs a genuine Linux kernel inside Windows, the container gains access to Linux-level GPU controls. [3, 4] 
Your automated workflow would look like this:

   1. Spin up the Docker container with hardware pass-through enabled (--device=/dev/dri). [3] 
   2. Inside that container, create your .venv (or use the container's global Python environment). [3, 5] 
   3. Run your automated dataset script. The Python subprocess call to rocm-smi or amdsmi will execute successfully. [6, 7] 

## Path B: Adjust Settings via Windows GUI (Easiest if skipping containers)
If you decide to stick purely to your native Windows .venv without a container, you must drop the automated script approach and handle the hardware caps manually via a GUI application.
Open AMD Software: Adrenalin Edition and configure these settings to mitigate the power surges:

* 
* Navigate to Performance > Tuning.
* Change the Tuning Control to Custom.
* Enable Power Tuning and pull the Power Limit (%) slider down to -10% or -15%.
* Enable GPU Tuning, turn on Advanced Control, and manually lower the Max Frequency (MHz) by roughly 100–200 MHz. This prevents the clock speed from spiking aggressively when a new audio bite queues up. [8] 
* 

## Path C: Windows 10 Registry Hack (Advanced Windows Automation)
If you must automate this natively on Windows within your current .venv, you cannot easily use Python code to change live wattage. Instead, you have to use a tool like MorePowerTool (MPT) to modify your GPU’s internal PowerPlay Tables (PP_Tables) in the Windows Registry. [9] 
By altering the registry, you permanently lower the maximum Total Board Power (TBP) allowed by the Windows graphics driver. This forces a hard hardware limit that stays active through every reboot, completely neutralizing the transient micro-surges without needing any script to run during execution. [9] 
Are you planning to shift your .venv workflow entirely inside a Docker container, or would you prefer guidance on configuring a stable manual profile within AMD Adrenalin to run alongside your native Windows .venv?

[1] [https://rocm.docs.amd.com](https://rocm.docs.amd.com/projects/amdsmi/en/latest)
[2] [https://wiki-biop.epfl.ch](https://wiki-biop.epfl.ch/en/ipa/deep-learning/python-venvs)
[3] [https://www.youtube.com](https://www.youtube.com/watch?v=RVT1iLShkJs)
[4] [https://medium.com](https://medium.com/@ericliux/how-i-turned-my-gaming-pc-into-a-local-ai-workstation-712f5e2d6962)
[5] [https://docs.lambda.ai](https://docs.lambda.ai/public-cloud/on-demand/managing-system-environment/)
[6] [https://rocm.docs.amd.com](https://rocm.docs.amd.com/projects/amdsmi/en/docs-6.2.4/)
[7] [https://stackoverflow.com](https://stackoverflow.com/questions/69240556/should-i-install-python-packages-as-non-root-user-or-set-up-a-virtual-environmen)
[8] [https://www.pcworld.com](https://www.pcworld.com/article/2054936/how-to-make-your-graphics-card-better-with-just-one-click.html)
[9] [https://www.reddit.com](https://www.reddit.com/r/linux_gaming/comments/1m4uawx/guide_modifying_amd_gpus_powerplay_tables_on/)
If you are running your script within a Python virtual environment (.venv) directly on native Windows 10, the provided Python code using rocm-smi will not work.
Because virtual environments share the host operating system's kernel, a Windows .venv can only execute standard Windows applications and libraries. It cannot run Linux binary tools like rocm-smi or amdsmi because the low-level AMD drivers for Windows do not expose those command-line interfaces. [1, 2] 
To execute automated power-capping scripts, choose one of the following approaches based on your preferred configuration:
------------------------------
## Path A: Run inside a WSL2 Container (Recommended for Automation)
If you containerize using Docker with the WSL2 backend (as detailed in the previous step), the container handles the heavy lifting. Because WSL2 runs a genuine Linux kernel inside Windows, the container gains access to Linux-level GPU controls. [3, 4] 
Your automated workflow would look like this:

   1. Spin up the Docker container with hardware pass-through enabled (--device=/dev/dri). [3] 
   2. Inside that container, create your .venv (or use the container's global Python environment). [3, 5] 
   3. Run your automated dataset script. The Python subprocess call to rocm-smi or amdsmi will execute successfully. [6, 7] 

## Path B: Adjust Settings via Windows GUI (Easiest if skipping containers)
If you decide to stick purely to your native Windows .venv without a container, you must drop the automated script approach and handle the hardware caps manually via a GUI application.
Open AMD Software: Adrenalin Edition and configure these settings to mitigate the power surges:

* 
* Navigate to Performance > Tuning.
* Change the Tuning Control to Custom.
* Enable Power Tuning and pull the Power Limit (%) slider down to -10% or -15%.
* Enable GPU Tuning, turn on Advanced Control, and manually lower the Max Frequency (MHz) by roughly 100–200 MHz. This prevents the clock speed from spiking aggressively when a new audio bite queues up. [8] 
* 

## Path C: Windows 10 Registry Hack (Advanced Windows Automation)
If you must automate this natively on Windows within your current .venv, you cannot easily use Python code to change live wattage. Instead, you have to use a tool like MorePowerTool (MPT) to modify your GPU’s internal PowerPlay Tables (PP_Tables) in the Windows Registry. [9] 
By altering the registry, you permanently lower the maximum Total Board Power (TBP) allowed by the Windows graphics driver. This forces a hard hardware limit that stays active through every reboot, completely neutralizing the transient micro-surges without needing any script to run during execution. [9] 
Are you planning to shift your .venv workflow entirely inside a Docker container, or would you prefer guidance on configuring a stable manual profile within AMD Adrenalin to run alongside your native Windows .venv?

[1] [https://rocm.docs.amd.com](https://rocm.docs.amd.com/projects/amdsmi/en/latest)
[2] [https://wiki-biop.epfl.ch](https://wiki-biop.epfl.ch/en/ipa/deep-learning/python-venvs)
[3] [https://www.youtube.com](https://www.youtube.com/watch?v=RVT1iLShkJs)
[4] [https://medium.com](https://medium.com/@ericliux/how-i-turned-my-gaming-pc-into-a-local-ai-workstation-712f5e2d6962)
[5] [https://docs.lambda.ai](https://docs.lambda.ai/public-cloud/on-demand/managing-system-environment/)
[6] [https://rocm.docs.amd.com](https://rocm.docs.amd.com/projects/amdsmi/en/docs-6.2.4/)
[7] [https://stackoverflow.com](https://stackoverflow.com/questions/69240556/should-i-install-python-packages-as-non-root-user-or-set-up-a-virtual-environmen)
[8] [https://www.pcworld.com](https://www.pcworld.com/article/2054936/how-to-make-your-graphics-card-better-with-just-one-click.html)
[9] [https://www.reddit.com](https://www.reddit.com/r/linux_gaming/comments/1m4uawx/guide_modifying_amd_gpus_powerplay_tables_on/)
No, AMD Software: Adrenalin Edition does not provide a native Command Line Interface (CLI) or a direct scripting utility on Windows 10. AMD handles their driver architecture with a strict separation of concerns: Windows gets the heavy consumer GUI, while Linux gets the scriptable amdsmi / rocm-smi command-line tools. [1, 2, 3] 
However, you can achieve automation inside your Python script on Windows 10 by bypassing Adrenalin entirely and leveraging the hardware directly using a couple of reliable workarounds.
## Workaround 1: Use an MSI Afterburner Batch Script (Best Solution)
While Adrenalin can't be scripted, MSI Afterburner can. It features hidden command-line switches that allow you to load pre-configured tuning profiles silently using standard Windows batch files or your Python automation script. [4] 

   1. Create the Stable Profile (Manual Step):
   Open MSI Afterburner. Cap your power limit slider, pull your clocks into a narrower band, and click the disk icon to save it into Profile 1.
   2. Automate with Python:
   Use your data generation Python script to activate this restricted profile right before jumping into the generation loop, and optionally restore default settings when the loop finishes.

import subprocessimport time
def set_gpu_profile(profile_number):
    try:
        # Pass the absolute path to your Afterburner executable
        # -profileX forces the GPU instantly into that hardware profile
        path = r"C:\Program Files (x86)\MSI Afterburner\MSIAfterburner.exe"
        subprocess.run([path, f"-profile{profile_number}"], check=True)
        print(f"Successfully applied GPU Profile {profile_number}")
    except Exception as e:
        print(f"Failed to apply hardware profile: {e}")
# 1. Apply your strict power-capped profile before the loop starts
set_gpu_profile(1) 
# 2. Run your ComfyUI voice generation loops here...
print("Running AI dataset loop safely...")
time.sleep(5) 
# 3. Optional: Trigger your default profile (e.g., Profile 2) when the script closes# set_gpu_profile(2)

## Workaround 2: The Adrenalin Profile "Hot-Swapping" Trick
If you prefer not to install third-party software like Afterburner, you can create a pseudo-automated workflow using Adrenalin's manual import/export options: [1] 

   1. Set your safe power limit and clock cap manually inside Adrenalin's Performance > Tuning tab.
   2. Click the Export Profile button (the small page icon) to save these custom safe settings to an .xml file (e.g., safe_power.xml).
   3. Reset Adrenalin to stock and save that config to a separate file (e.g., gaming_stock.xml). [1, 5] 

While you cannot script the actual activation of these XML files natively, keeping these files on your desktop allows you to manually hot-swap the card's entire hardware configuration in exactly two clicks before launching your .venv script, avoiding manual slider configuration every time.
## Why This Protects Your Hardware Across the .venv Split
Because your main Python automation script runs on native Windows, executing an MSI Afterburner CLI command from that environment handles hardware limitations perfectly. When your script pings the ComfyUI API endpoint over in WSL2, the host-level power restriction will already be clamped directly onto the GPU silicon. This guarantees that when PyTorch hits the chip, the transient power surge is clipped before it can reach your PSU's safety switches.
If you run into issues finding your MSI Afterburner installation path or need help verifying that the profile applied correctly, let me know!

[1] [https://www.youtube.com](https://www.youtube.com/watch?v=mpVF7jVAO_o)
[2] [https://instinct.docs.amd.com](https://instinct.docs.amd.com/projects/amd-smi-virt/en/latest/how_to/amdsmi_cli_usage.html)
[3] [https://rocm.docs.amd.com](https://rocm.docs.amd.com/projects/amdsmi/en/docs-5.7.1/amdsmi_cli_readme_link.html)
[4] [https://www.reddit.com](https://www.reddit.com/r/streamdeckprofiles/comments/pbhjti/automate_loading_amd_overclock_profile/)
[5] [https://www.amd.com](https://www.amd.com/en/resources/support-articles/faqs/DH3-020.html)
