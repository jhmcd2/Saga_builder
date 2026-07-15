Using your existing CPU, cooler, memory, and power supply means your setup is nearly complete. You only need to purchase the replacement motherboard and the remaining 8 hard drives.
TrueNAS has a steep learning curve and its rigid ZFS file system can cause initialization failures if the hardware or drive mapping isn't perfect. To get a sleek, "point-and-click" web dashboard experience similar to Synology's DiskStation Manager (DSM) on your custom hardware, you have two primary options: Unraid or Xpenology. [1, 2, 3] 
------------------------------
## Choosing Your Synology Alternative## 1. Unraid (Recommended)
Unraid is widely considered the absolute best software for DIY media servers. It features a clean, highly visual web dashboard and an integrated, one-click App Store that mimics Synology's Package Center. [2, 3, 4] 

* The App Experience: It uses Docker behind the scenes, allowing you to install Plex, Radarr, or file sync utilities with a single click.
* The SAS Drive Advantage: Unraid does not use traditional strict RAID. It uses a unique "Parity" system, allowing you to mix and match different drive sizes over time. If you buy an 8TB SAS drive today and a 14TB SAS drive next year, Unraid will pool them seamlessly.
* Cost: It requires a one-time paid license tied to your boot USB flash drive. [3, 5] 

## 2. Xpenology (The Clone Option)
Xpenology is a community-driven bootloader that allows you to run Synology’s actual operating system (DSM) on non-Synology hardware. [1] 

* The Experience: It is an exact 1:1 match to a real Synology because it is running the actual Synology software.
* The Catch: It is a community hack. An official Synology software update can break your system or cause data to become temporarily inaccessible until the community releases a new bootloader patch. It requires technical troubleshooting and is not recommended if you want a stable, "set-and-forget" machine. [1, 6] 

------------------------------
## Finalizing Your Hardware Shopping List## 1. The Replacement Motherboard
Since your previous Supermicro board was an LGA 2011-3 with a Narrow ILM layout, you need to match those exact sockets so your processor and cooler fit perfectly. Look for these specific models on the secondhand market:

* Supermicro X10SRi-F or X10SRL-F (~$80 – $110 on eBay)
* Why: These are single-socket, standard ATX motherboards that accept your server memory and Xeon chip. They feature the exact Narrow ILM layout required for your cooler and offer enough PCIe slots to house your HBA and 10GbE network card simultaneously.

## 2. Sourcing 8 More Used SAS Drives
Because you are using an LSI HBA card, you want enterprise-grade 3.5" SAS 12Gb/s drives. Look for reputable enterprise refurbishers on eBay or specialized server hardware stores rather than individual random sellers.

* Target Models: HGST He10/He12, Seagate Exos X14/X16, or Dell/HP enterprise-labeled drives.
* Pricing Target: Used 12TB to 14TB enterprise SAS drives regularly sell for $75 to $95 each with vendor warranties.

------------------------------
## Critical Assembly Safety Check
Before installing your replacement Supermicro board into the [Rosewill Hearth NAS Pro chassis](https://www.rosewill.com/rosewill-hearth-nas-black/p/9SIA072KVM9039), you must physically check the case's brass motherboard standoffs.
Enterprise server boards often place their mounting holes in slightly different positions than standard consumer ATX desktop boards. If a pre-installed brass standoff lines up with bare metal traces on the back of your Supermicro board instead of a designated screw hole, it will create a hard short circuit the moment you turn on the power supply. Unscrew and remove any standoffs that do not perfectly align with a motherboard mounting hole.
If you want to move forward with the purchase, let me know:

* What capacity (e.g., 10TB, 14TB) are your 4 existing SAS drives?
* Do you want help checking a specific eBay motherboard listing to confirm it has the correct Narrow ILM socket for your cooler?


[1] [https://www.reddit.com](https://www.reddit.com/r/synology/comments/1r729tw/any_good_alternatives_to_synology/)
[2] [https://www.lincplustech.com](https://www.lincplustech.com/it/blogs/news/best-nas-operating-system-2025)
[3] [https://www.xda-developers.com](https://www.xda-developers.com/open-source-os-options-for-nas/)
[4] [https://www.lincplustech.com](https://www.lincplustech.com/news/best-nas-operating-system-2025.html)
[5] [https://linuxcommunity.io](https://linuxcommunity.io/t/synology-nas-alternatives/5174)
[6] [https://www.youtube.com](https://www.youtube.com/watch?v=NDuWQ9HEkF4&vl=en&t=693)
Using your existing CPU, cooler, memory, and power supply means your setup is nearly complete. You only need to purchase the replacement motherboard and the remaining 8 hard drives.
TrueNAS has a steep learning curve and its rigid ZFS file system can cause initialization failures if the hardware or drive mapping isn't perfect. To get a sleek, "point-and-click" web dashboard experience similar to Synology's DiskStation Manager (DSM) on your custom hardware, you have two primary options: Unraid or Xpenology. [1, 2, 3] 
------------------------------
## Choosing Your Synology Alternative## 1. Unraid (Recommended)
Unraid is widely considered the absolute best software for DIY media servers. It features a clean, highly visual web dashboard and an integrated, one-click App Store that mimics Synology's Package Center. [2, 3, 4] 

* The App Experience: It uses Docker behind the scenes, allowing you to install Plex, Radarr, or file sync utilities with a single click.
* The SAS Drive Advantage: Unraid does not use traditional strict RAID. It uses a unique "Parity" system, allowing you to mix and match different drive sizes over time. If you buy an 8TB SAS drive today and a 14TB SAS drive next year, Unraid will pool them seamlessly.
* Cost: It requires a one-time paid license tied to your boot USB flash drive. [3, 5] 

## 2. Xpenology (The Clone Option)
Xpenology is a community-driven bootloader that allows you to run Synology’s actual operating system (DSM) on non-Synology hardware. [1] 

* The Experience: It is an exact 1:1 match to a real Synology because it is running the actual Synology software.
* The Catch: It is a community hack. An official Synology software update can break your system or cause data to become temporarily inaccessible until the community releases a new bootloader patch. It requires technical troubleshooting and is not recommended if you want a stable, "set-and-forget" machine. [1, 6] 

------------------------------
## Finalizing Your Hardware Shopping List## 1. The Replacement Motherboard
Since your previous Supermicro board was an LGA 2011-3 with a Narrow ILM layout, you need to match those exact sockets so your processor and cooler fit perfectly. Look for these specific models on the secondhand market:

* Supermicro X10SRi-F or X10SRL-F (~$80 – $110 on eBay)
* Why: These are single-socket, standard ATX motherboards that accept your server memory and Xeon chip. They feature the exact Narrow ILM layout required for your cooler and offer enough PCIe slots to house your HBA and 10GbE network card simultaneously.

## 2. Sourcing 8 More Used SAS Drives
Because you are using an LSI HBA card, you want enterprise-grade 3.5" SAS 12Gb/s drives. Look for reputable enterprise refurbishers on eBay or specialized server hardware stores rather than individual random sellers.

* Target Models: HGST He10/He12, Seagate Exos X14/X16, or Dell/HP enterprise-labeled drives.
* Pricing Target: Used 12TB to 14TB enterprise SAS drives regularly sell for $75 to $95 each with vendor warranties.

------------------------------
## Critical Assembly Safety Check
Before installing your replacement Supermicro board into the [Rosewill Hearth NAS Pro chassis](https://www.rosewill.com/rosewill-hearth-nas-black/p/9SIA072KVM9039), you must physically check the case's brass motherboard standoffs.
Enterprise server boards often place their mounting holes in slightly different positions than standard consumer ATX desktop boards. If a pre-installed brass standoff lines up with bare metal traces on the back of your Supermicro board instead of a designated screw hole, it will create a hard short circuit the moment you turn on the power supply. Unscrew and remove any standoffs that do not perfectly align with a motherboard mounting hole.
If you want to move forward with the purchase, let me know:

* What capacity (e.g., 10TB, 14TB) are your 4 existing SAS drives?
* Do you want help checking a specific eBay motherboard listing to confirm it has the correct Narrow ILM socket for your cooler?


[1] [https://www.reddit.com](https://www.reddit.com/r/synology/comments/1r729tw/any_good_alternatives_to_synology/)
[2] [https://www.lincplustech.com](https://www.lincplustech.com/it/blogs/news/best-nas-operating-system-2025)
[3] [https://www.xda-developers.com](https://www.xda-developers.com/open-source-os-options-for-nas/)
[4] [https://www.lincplustech.com](https://www.lincplustech.com/news/best-nas-operating-system-2025.html)
[5] [https://linuxcommunity.io](https://linuxcommunity.io/t/synology-nas-alternatives/5174)
[6] [https://www.youtube.com](https://www.youtube.com/watch?v=NDuWQ9HEkF4&vl=en&t=693)
That explains why the previous build was totaled. A damaged PCIe slot along with surface trace scratches can easily short a board or break the communication lines between the CPU and the rest of the hardware.
Since you already have the SAS cables, the 750W power supply, the 10GbE network card, and the Supermicro Narrow ILM CPU cooler, your shopping list is incredibly short.
## The Replacement Plan
You just need to replace the damaged motherboard, get a compatible CPU, and add some cheap server RAM. Since you already have the specific Supermicro cooler, sticking to an LGA 2011-3 Supermicro board ensures a perfect fit. [1, 2] 
Here is the exact hardware stack to look for on the secondhand market to complete the build inside the [Rosewill Hearth NAS Pro Enclosure](https://www.newegg.com/rosewill-hearth-nas-pro-black/p/N82E16811147396):

* Replacement Motherboard: Supermicro X10SRi-F or X10SRL-F (~$80 - $110 on eBay)
* Why: These are single-socket LGA 2011-3 ATX motherboards. They feature the exact same Narrow ILM mounting pattern for your existing cooler, use standard ATX power connections, and offer plenty of physical PCIe slots to host your 10GbE card and SAS HBA.
* Processor: Intel Xeon E5-2630 v4 or E5-2650 v4 (~$10 - $20 on eBay)
* Why: These 10-to-12-core chips cost next to nothing used, idle at incredibly low power wattages, and natively support the 40 PCIe lanes needed to handle your high-speed networking and storage cards without bottlenecking.
* Memory: 32GB or 64GB DDR4 ECC Registered RDIMM (2133MHz or 2400MHz) (~$30 - $55 on eBay)
* Why: You must use Server ECC Registered RAM with these X10 boards (standard desktop RAM will not boot). It is highly affordable on the secondhand market and protects your 12-bay array against data corruption.
* SAS Controller (HBA): LSI Broadcom 9300-8i (Flashed to IT Mode) (~$30 - $45 on eBay)
* Why: You will need this PCIe card to plug your existing SAS cables into. It bridges the motherboard to your secondhand SAS drives.

## Assembly Notes for the Hearth NAS Pro

   1. Standoff Alignment: Before mounting the replacement Supermicro board into the Hearth NAS Pro chassis, check the pre-installed brass motherboard standoffs. Enterprise boards occasionally feature slightly shifted mounting holes compared to standard consumer layout templates. Ensure there are no stray standoffs touching the bare underside of the board. [3, 4] 
   2. PCIe Slot Selection: Pop your 10GbE card into the top slot and the SAS HBA into the slot right below it. This spaces them out to optimize the built-in airflow of the Rosewill case, keeping the controller chips cool during heavy data transfers.

Are you planning to run TrueNAS Scale or Unraid as the operating system for this 12-bay pool?

[1] [https://www.reddit.com](https://www.reddit.com/r/buildapc/comments/1e0qzs7/fixing_a_dead_cpu/)
[2] [https://www.reddit.com](https://www.reddit.com/r/MSI_Gaming/comments/1it8g5x/x870e_motherboards_m2_1_bug_data_collection/)
[3] [https://digibuggy.com](https://digibuggy.com/blog-details/10-common-mistakes-in-gaming-pc-builds-and-how-to-fix-them)
[4] [https://www.reddit.com](https://www.reddit.com/r/buildapc/comments/8ab9bo/my_pc_wont_boot_up_after_disassembling_and/)
Having existing cables, a functional 750W Bronze PSU, and an available 10GbE network card cuts your DIY hardware expenses significantly. [1] 
The CPU and cooling configuration requires specific alignment with Supermicro hardware parameters. [2] 
## Understanding the Motherboard Layout (Narrow ILM)
Supermicro enterprise motherboards (specifically the dual-socket LGA 2011 and LGA 2011-3 boards used for Xeon v4 processors) do not use standard desktop square CPU mounting layouts. Instead, they feature a Narrow ILM (Independent Loading Mechanism), which is a rectangular mounting grid measuring roughly 2.2" x 3.7".
A standard consumer CPU cooler will not align with these mounting holes. A specific Narrow ILM cooler is required for compatibility:

* The "-L" Modifier Meaning: In Supermicro nomenclature, an "-L" suffix on an active cooler assembly or a system component denotes a Low-Profile or specific low-clearance design. [3] 
* Case Compatibility: The [Rosewill Hearth NAS Pro](https://www.rosewill.com/rosewill-hearth-nas-black/p/9SIA072KVM9039) is a standard mid-tower design. It provides enough vertical clearance to accommodate standard tower coolers, avoiding the height restrictions common in shallow rackmount enclosures.
* Sourcing the Correct Cooler: Avoid modifying standard desktop brackets, which can cause uneven mounting pressure and potential CPU contact issues. Native choices include:
* Noctua NH-D9DX i4 3U on Amazon: A quiet active tower cooler that includes integrated, out-of-the-box hardware brackets for Intel LGA2011/2011-3 Narrow ILM configurations.
   * [Supermicro SNK-P0050AP4 on eBay](https://www.ebay.com/itm/178054858559): The official OEM active 2U/4U cooling solution designed specifically for LGA2011-3 Narrow ILM boards. It provides reliable cooling capacity but produces more audible fan noise than consumer alternatives. [3, 4, 5, 6] 

## Hardware Reusability & Networking

* Power Supply: Your 750W 80+ Bronze PSU provides sufficient capacity. While a Gold-rated unit offers better power efficiency for 24/7 operations, 750W easily handles the power draw of a Xeon v4 processor, an HBA card, a 10GbE adapter, and the combined spin-up current of 12 SAS hard drives.
* Cabling: Standard SATA breakout cables cannot interface with SAS drives. Since you already possess the proper SFF-8482 cables (which feature the unified data and power block required by SAS drives), you can connect them directly to your LSI HBA card without buying new adapters.
* Network Isolation: Connecting a 10GbE server port directly to a 10GbE uplink port on an HP Procurve switch functions correctly, even if the remaining network clients operate on standard 1GbE links. The switch handles the internal data rate conversion. Data traffic moving between your main workstation and the server will utilize the maximum available speed of the slowest link in that specific path. [7] 

If you want to finalize the setup, tell me:

* What is the exact model number of the Supermicro motherboard you own?
* Do you know the exact Xeon processor model number currently installed on it?
* What desktop or workstation will you use to connect to this server?


[1] [https://www.xda-developers.com](https://www.xda-developers.com/things-to-fill-empty-pcie-slots-motherboard/)
[2] [https://www.reddit.com](https://www.reddit.com/r/HomeServer/comments/xv9mvg/repurpose_old_supermicro_case/)
[3] [https://www.reddit.com](https://www.reddit.com/r/homelab/comments/12b0ca7/narrow_ilm_lga2011v3_cooler_for_tower_case_that/)
[4] [https://www.truenas.com](https://www.truenas.com/community/threads/cryorig-h7-cooler-in-supermicro-server-now-updated-with-photos-and-information-5-12-2021.89896/)
[5] [https://www.ebay.com](https://www.ebay.com/itm/178054858559)
[6] [https://www.ebay.com](https://www.ebay.com/itm/285712892325)
[7] [https://www.newsshooter.com](https://www.newsshooter.com/2018/06/07/upgrade-10gbe-new-sonnet-solo-10g-pcie-card/)
An enterprise-grade secondhand spec sheet for the [Rosewill Hearth NAS Pro Chassis](https://www.rosewill.com/rosewill-hearth-nas-black/p/9SIA072KVM9039) utilizes older datacenter parts. These components are highly reliable, offer massive PCIe lane capacity for SAS controllers, and are readily available on the used market. [1, 2] 
## Used Part Breakdown (Estimated Total: ~$440 - $550)

* Motherboard + CPU Combo: Intel Xeon E5-2600 v4 (e.g., E5-2650v4 or E5-2680v4) with an X10 Series Supermicro Motherboard (~$150 - $180 on eBay)
* Why: Supermicro enterprise motherboards fit perfectly in the [Hearth NAS Pro E-ATX layout](https://www.rosewill.com/rosewill-hearth-nas-black/p/9SIA072KVM9039). Xeon v4 chips offer 12 to 14 cores, which efficiently handle heavy storage tasks, plex transcoding, or docker containers while idling at low power. Crucially, they provide 40 PCIe lanes to easily run SAS cards and 10GbE networking simultaneously. [1, 2] 
* Memory: 64GB (2 x 32GB) DDR4 ECC Registered RAM (~$60 - $80 on eBay)
* Why: Used enterprise server RAM is incredibly cheap. TrueNAS and unRAID thrive on RAM cache, and ECC (Error-Correcting Code) memory prevents data corruption across your 12-drive array. [2, 3, 4] 
* SAS Controller (HBA): LSI Broadcom 9300-16i or 9300-8i (Pre-Flashed to IT Mode) (~$45 - $70 on eBay)
* Why: This card acts as the "translator" that lets standard motherboards communicate natively with enterprise SAS drives. Ensure the listing explicitly says "IT Mode" or "Target Mode" so your operating system can see each individual drive directly without hardware RAID interference. [5, 6] 
* Cables: Mini-SAS HD (SFF-8643) to SAS 29-Pin (SFF-8482) Breakout Cables (~$25 for a pack)
* Why: Standard SATA cables will not fit SAS drives. You need SFF-8482 cables, which feature data pins and integrated power connectors specifically designed for enterprise SAS drives. [5] 
* Network Card: Intel X520-DA2 or Solarflare SFN7122F Dual-Port 10GbE SFP+ Card (~$25 - $35 on eBay)
* Why: A 12-drive array easily saturates standard 1GbE network lines. A used enterprise 10Gb network card gives you lightning-fast transfer speeds for editing or backing up directly to the server.
* Power Supply: EVGA, Corsair, or Seasonic 750W 80+ Gold Certified PSU (~$55 - $70 on eBay / Local Used)
* Why: Hard drives require significant power when spinning up all at once. A reputable 750W power supply handles the initial power spike easily. Do not buy a used generic power supply; stick to trusted brands to protect your data. [2, 7, 8] 
* Boot Drive: Used Samsung PM983 or Intel enterprise NVMe M.2 / U.2 SSD (256GB - 512GB) (~$20 - $30)
* Why: High endurance enterprise boot drives ensure your server OS stays up continuously without the failure rates associated with cheap consumer flash drives.

## System Scalability & Fit
The Rosewill Hearth NAS Pro provides an intentional edge for this specific setup because it supports dual ATX power supplies. If you decide to add an expansion disk shelf in the future to hold even more SAS drives, you can drop a second cheap, used power supply into the chassis to handle the extra load without replacing your primary system. [1] 
Would you like to focus on tracking down these parts on eBay, or should we look at configuring a TrueNAS vs. unRAID storage pool layout for the SAS drives?

[1] [https://www.rosewill.com](https://www.rosewill.com/rosewill-hearth-nas-black/p/9SIA072KVM9039)
[2] [https://www.techpowerup.com](https://www.techpowerup.com/forums/threads/rosewill-announces-the-hearth-series-nas-server-chassis.348542/)
[3] [https://www.reddit.com](https://www.reddit.com/r/HomeNAS/comments/1514apy/diy_nas_build_opinionsadvice_needed/)
[4] [https://www.reddit.com](https://www.reddit.com/r/homelab/comments/umq94g/used_server_or_just_build_a_pc/)
[5] [https://www.youtube.com](https://www.youtube.com/watch?v=HrgX_rESC14&t=120)
[6] [https://www.youtube.com](https://www.youtube.com/watch?v=vPyE--ecX7k&t=632)
[7] [https://www.reddit.com](https://www.reddit.com/r/buildapc/comments/r1ub7w/looking_to_save_money_are_used_components_a_nogo/)
[8] [https://prerackit.com](https://prerackit.com/the-rise-of-counterfeit-enterprise-hard-drives-how-to-spot-fake-drives-before-its-too-late/)
