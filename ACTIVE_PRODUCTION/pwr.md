Having 2 native Molex connectors and 7 SATA ports all sharing a single hardwired ribbon gives you a much better layout than a single lone line, but it still requires careful balancing to run 12 power-hungry SAS drives safely.
Your non-modular layout means you are working with exactly two physical wire channels coming out of the PSU box: the Native Molex channel (2 plugs) and the Native SATA channel (7 plugs). You need to divide the 12-drive load between these two channels as evenly as possible.
## Your Best Options for Connection Mapping## Option A: Maximize Both Hardwired Lines (Recommended)
This is the cleanest strategy. You will load 6 drives onto the Molex wire run and 6 drives onto the SATA wire run. This splits the initial 300W+ boot-up spin-up spike perfectly down the middle, keeping both runs below the dangerous 150W threshold.

   1. On the Native Molex Run (6 Drives Total):
   * Take your 2 native Molex plugs.
      * Attach a Molex Male to Dual Molex Female Y-Splitter to each of them. You now have 4 Molex plugs.
      * Plug 4 of your SFF-8482 cables directly into those.
      * For the final 2 drives on this run, use a 3-way or 4-way Molex daisy-chain splitter to expand one of the heads.
   2. On the Native SATA Run (6 Drives Total):
   * You have 7 native SATA plugs, but you will only use 6 of them. Leave the 7th plug completely empty to avoid overloading the line.
      * On those 6 active SATA plugs, attach an individual StarTech SATA Male to 4-Pin Molex Female Adapter or a Nippon Labs SATA-FM Adapter to each one.
      * Plug your remaining 6 SFF-8482 cables into those adapted sockets.
   
## Option B: Tap into Your Extra PCIe Cables (Safest Electrically)
If you want absolutely zero risk of pulling too much current over the storage wires, remember that an 850W non-modular PSU has multiple thick 8-pin PCIe cables hanging off it meant for power-hungry GPUs. You can offload 4 of your drives entirely onto a completely separate, heavily reinforced 12V rail.

* 
* modDIY PCIe 8-Pin to Dual Molex Female Adapters: The PCIe Run (4 Drives): Buy two modDIY PCIe 8-Pin to Dual Molex Female Adapters. Plug them directly into two of your unused graphics card power cables. This gives you 4 heavy-duty Molex plugs completely separate from your storage lines.
* The Native Molex Run (2 Drives): Plug 2 of your SFF cables straight into the 2 native Molex plugs coming off the PSU string.
* StarTech 15-pin to 4-pin adapters: The Native SATA Run (6 Drives): Convert 6 of the 7 ports on your SATA strip using individual StarTech 15-pin to 4-pin adapters.
* 

## 🛑 Crucial Wiring Golden Rules for This Setup

* 
* Do NOT use a SATA 1-to-4 splitter on the SATA strip: Since all 7 SATA ports are already sharing one thin wire backbone, trying to add a "1-to-4 SATA splitter" onto one of those plugs to generate more connections will melt the main ribbon. Stick to 1-to-1 conversions on the SATA plugs.
* Avoid Molded Plastic SATA Plugs: When buying your SATA-to-Molex adapters, ensure the SATA male end uses a hard plastic clip-together body with crimped metal pins. Completely avoid the soft, smooth, injection-molded rubber/plastic type.
* 

Which layout looks more manageable for the space inside your case: Option A (splitting the Molex and using 6 of your SATA heads) or Option B (hooking adapters into your hanging PCIe graphics card wires)?

