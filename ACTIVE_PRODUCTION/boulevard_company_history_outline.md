# Boulevard Company History – Outline

Nov 8, 2026 · @James McDonald

## 1. Purpose

`company_history.py` holds Boulevard's scripted story: events on fixed dates or in fixed eras, layered on top of the general rules in `fleet_builder.py`.

- **What it can change today:** money and contracts only (cash, loans, leases, how aircraft are paid for).
- **What it cannot change today:** demand, operations, costs per flight, the fleet, or the network.
- **Called from** `simulation.run_day` in three places:
  1. Exact-date story events (the Japanese loan, the MetroJet shutdown), before anything else that day
  2. Boulevard Holding income, on the 1st of each month
  3. `preferred_acquisition_mode` and the MetroJet lender name, used by `fleet_builder.py` when buying or leasing aircraft

## 2. Story timeline (as written in the file)

- **Before 1995:** East Coast Part 91K / Part 135 charter operation; grows enough to partner with US Airways. *Not simulated.*
- **1995:** Part 121 scheduled service launches as MetroJet, flown under the US Airways brand to avoid pilot scope clauses (sued anyway). Fleet of 737-200s leased from US Airways.
- **1998:** Most legacy types (DC-10, F100, 727-200, L-1011, MD-80, 737-900) arrive on a favorable Boeing "starter package" lease. A340 and MD-11 excluded. *Not built.*
- **Early 2001:** Growth trap and near insolvency. A Japanese investment company's loan refinances the debt.
- **30 Oct 2001:** US Airways shuts down MetroJet amid its bankruptcy. Boulevard absorbs the leased 737-200s outright.
- **2012:** Boulevard Holding formed: outside investments, including Maryland land made valuable by BRAC redevelopment, subsidize the airline (Emirates/Dubai pattern).
- **2015:** Healthy enough to pay cash for aircraft again.
- **2020 (COVID):** Reserves, Holding income, rolling furloughs and management pay cuts keep it solvent; controlled ramp-up afterward. *Not built.*

## 3. Events currently built

1. **MetroJet launch (first day simulated, 1995)** — lives in `fleet_builder.py`, not this file
   - $50M starting capital
   - 50 used 737-200s, capped at 49 by the type's fleet limit, split evenly across the active hubs (BWI, FLL)
   - Leased from "US Airways / MetroJet" at 0.8% of aircraft value per month
2. **Japanese refinancing loan (1 Feb 2001)**
   - $200M; 5% a year; 15 years; interest only (about $833k a month)
   - Pays off existing aircraft loans, oldest first; one it can't fully cover is partly paid down (monthly payment not recalculated)
   - Money left over becomes cash
   - Full $200M due in one payment on 1 Feb 2016
3. **MetroJet shutdown (30 Oct 2001)**
   - Every lease still with "US Airways / MetroJet" is closed as "Transferred"
   - Boulevard keeps the aircraft at no cost; lease payments stop
   - $0 ledger entry records it
4. **Boulevard Holding income (from 2012, 1st of each month)**
   - 2012: $0.5M a month
   - 2015: $1.5M a month
   - 2018: $3M a month
   - 2022: $5M a month
5. **How aircraft are paid for**
   - Before 2015: loans (15% down, 7%, 10 years)
   - From 2015: cash, falling back to a loan when cash is short

**Rule to know:** the two 2001 events fire only if the simulation runs on that exact date. A run that starts later skips them.

## 4. In the story but not built

- **Pre-1995 charter years** — background only; the simulation starts in 1995.
- **1998 Boeing starter package**
  - Types: DC-10, F100, 727-200, L-1011, MD-80, 737-900 (not A340, not MD-11)
  - Needs: how many of each type, which hubs, lease terms, delivery dates
- **COVID operations (2020)**
  - Rolling furloughs and management pay cuts → lower crew costs
  - Schedule contraction, then a controlled ramp-up
  - Today 2020 shows up only as a whole-year drop in the world passenger index
- **Voluntary principal paydowns on the Japanese loan** — the function exists in `fleet_builder.py` but nothing calls it, so the full $200M always falls due in 2016.

## 5. Story pieces in other files

- **Economic eras** (`seed_economic_factors.py`) — pilot/FA pay, maintenance costs, fare margin
  - 1995: budget carrier competing with Southwest/ValueJet (pilots $60/h, FAs $25/h, margin 1.05)
  - 2001: "accidentally" mainline scale (pilots $120/h, FAs $45/h, margin 1.20)
  - 2019: Emirates/Singapore-tier repositioning (pilots $180/h, FAs $70/h, margin 1.35)
  - Real U.S. jet fuel prices 1995–2020; years after 2020 reuse 2020's
- **Hubs and focus cities** (`specail_airports` data) — opening year per airport, e.g. BWI and FLL 1995, BOS 1999, IAD 2002, DTW and LAX 2003, Tokyo/Osaka 2008, Paris/London 2012; Hong Kong 2015, retired 2020
- **Aircraft eras** (aircraft data) — delivery and final-retirement dates and fleet caps per type; all trijets forced out by 2020 (`economics.py`)
- **Freighter conversions** (`cargo.py`) — 767-400 and 747-400 become freighters once 787s / 747-8s match their numbers; deadline 31 Dec 2026
- **Cargo timeline** (`cargo.py`) — operations start 2015, full capacity 2020 (COVID spike); lanes and contracts defined, no cargo flights scheduled yet
- **Demand history** (`routes.py`) — world passenger index with real figures from 2014; earlier years extended at steady growth (no 2001 or 2008 dip)

## 6. Events to add

**Template — one per event:**

- Name:
- Start date / end date:
- Where it hits: everywhere / US only / region / specific airports
- Airspace or airport closure (days, which airports):
- Demand change: how deep, and the recovery shape and length
- Cost changes (security, insurance, fuel, crew pay), with amounts and dates:
- Cash in or out (aid, grants, loans, fines):
- Fleet effects (groundings, retirements, deliveries):
- How Boulevard responds (capacity cut, mid-year re-plan, furloughs):
- Linked story events (e.g. MetroJet shutdown):

**Candidates:**

- [ ] 9/11 (11 Sep 2001) — closure, demand collapse and recovery, security and insurance costs, federal stabilization aid
- [ ] 1998 Boeing starter package (from section 4)
- [ ] 2008 fuel spike
- [ ] 2008–09 recession
- [ ] 2019 737 MAX grounding
- [ ] COVID 2020 operations (from section 4)

## 7. Placeholders and open questions

- [ ] Japanese loan date (1 Feb 2001) and amount ($200M) — placeholders; the story says only "early 2001"
- [ ] Boulevard Holding monthly income figures — placeholders
- [ ] Starting capital ($50M) — placeholder
- [ ] MetroJet shutdown date: keep 30 Oct 2001, or tie it to the 9/11 event? (Historically MetroJet ended around December 2001 — to verify.)
- [ ] Should the 1995 cold start split the fleet by demand instead of evenly across hubs? (12 FLL aircraft sat unused in 1995.)
- [ ] Boulevard market share (25%) — placeholder; per year or per market?
- [ ] Should a voluntary paydown of the Japanese loan be part of the story?
- [ ] Freighter conversion deadline for the 747-8 (2030) — marked unconfirmed in `cargo.py`





## Updated History

1. 1990 (NOT SIMULATED)
- Mid Atlantic Charter Started with a single B1900 mainly ran flights between DC and NYC as a Part 91K operator
- Mid Atlantic Charter establishes baltimore as a Hub
2. 1991 (NOT SIMULATED)
- Mid Antatlic Charter Applies for Part 135 Certificate, since the paper work is msotly the same opts to apply for Part 121 
2. 1992 (NOT SIMULATED)
- Now w/ 4 aircraft Mid Atlantic Charter takes over a number of essentual air routes in Maryland and PA.
3. 1993 (NOT SIMULATED)
- Mid Altantic Charters achieves Part 135 certificate, applies for Part 121 Certificate.
- Get's Partnership wwith US Airways
- US Airways pitches Metrojet idea
- An NYC investment Firm fund the joint venture. With US Airways as a guarentter, but Boulevard accepts all debt  
- Mid Atlantic Airways is Born
4. 1994 (NOT SIMULATED)
- Mid Atlantic Airways accepts former US Airways retired 737-200s
- The operation will be known publically as Metrojet, and will be a charter operatin, not a regional carrier.  
- Bouelvard hires first pilots.
- FAA grnats Mid Altantic Airways Part 121 Certicate, flights to start October 1, 1995
- Baltimore, Fort Lauderdale, La Guardia and Washignton National named hubs
5. 1995
- ALPA files information of request eventually supenaing Mid Altantic Airwyas.
- ALPA files injuction to keep the first aircraft from flying. It is denied. 
- ALPA sues Mid Atlantic Airways and US Airlines
- October 1, 1995, first Metrojet flight leaves from Jacksonville, FL to BWI Airport
- The publicity actually makes the airline a hit
- November 5, a 732 experiences total hydraulic falure on approach to BWI. THe aircraft land safely.  Cause determined to be lines were clogged.
6. 1996
- Jan 5, discovery begins in ALPA alwsuite
- Feb 1, a 737-200 experiences uncontrolled engien failure.  Upon landing the aircraf wsas evacuated onto the runway.  Owner of the company not trusting the MX records US Airways gave him, grounded the fleet for a month until the records could be confirmed. Turns out most aircraft had missed MX checks.  The fleet was fixed at cost by US Airways.  
- Feb 24, after the airline Owner demands fixes from US Air, the FAA stepped in and order teh fixes as well.  The timing caused many people to recommend the airline, as it became clear it was prioritizing safety
- March 10, first 10 737-200s return to the skies
- March 18, ALPA lawsuite over scope clause violation goes to trail.
- April 15, remainder of 737 fleet returns to the skies
- April 30, ALPA loses the lawsuite, HOWEVER, the judge states to qualify as an Part 121 Charter operation it much have at least 1 121 flight under its own brand.  The airline lied and stated that was always the planbut this offer required their full attention. The judge gave the airline 2 years.
- June 5, The start of creating a new route network begins.  They airline exploits existing connections to make the carrier start, but notes it cannot use andy US Airways equiment for this.  It is decided that isntead of one flight for legal reasons thaty would opt to make a fully functional airline
- August 5th, Boeing New Airline Program, a very fledgling operation  (Bouelvard is their first customer) is brought in to consult. 
- August 20, at the culstultant's insetence Mid Atlatnic Airline establishes the DBA of Bouelvard Airlines. The Name is eventually establlished as the full corproate name. 
- November 10, Boeing maps out an aggresive growth strategy that will take the company nation wide within months.  Captializing on the success of Metrojet.  Bouelvard will be a slightly preium airlines, providing budget fairs but with first class and amenities of mainline carriers and a first class. 
- December 11, Bouelvard Meets with another consulting grop just to get a second opinion.  They discover a second ariline si gearing up with some billionaires help called New Air. New Air aims to be a premium carrier also with massive trans con plans, and is already in negotiations to pruchase aircraft.  They state that Airbus has already offered them the A320 at a lower than average price.  
- Jan 3: Bouelvard enters aircraft negotiation, the preference is to stick with the 737, but the current line is already claimed out well into 1998. 
- Jan 4: ALPA enters into contract negotiates with the air carriers for their renewal.  
- Feb 10: It is leaked that ALPA plans on trying to Torpedo Metrojet or force it to be brought back in house at US Airways
- June 10:  Boeing's negotiatiosn get tough.  Boulevard opts to travel to France to meet with Airbus.  Were they tour the plant.  However, Airbus prices are far more than Boeings and they are unwilling to negotiate.  However they get the hint that they seem nto to take them seriuously. 
- August 5: they meet with the other consultant group. New Air is having an eaier time negotiating with Airbus.  
- October 10: Boeign offers a compromise, as a number of AA 727s are being change out with 737s they offer then a mixed bag fleet.  Older 727s but with glass cockpits, along with some Fokker 100s.  In a play they ask about aircraft for the rest of their growth plans.  Being ends up promising a sort of frankin fleet of DC-10s, F100, 727s, L1011, MD80s at reduced mx cost.  
- OCtober 20: Boulevard signs onto the frankin fleet but with one addition, brand new 737NG aircraft Intially it the type is left open.
7. 1997
Feb 5: ALPA redfines the scope clause, however, MetroJet cna continue operating as is as the new framework allows it to be defined as a codeshare arrangement 
- July 5: the neo green livary of the airline is estagblished and paint starts.  All new policy also states all incoming used aircraft must undergo D-checks regardless of what its last MX call was
- October 15: In order to make up for laggin sales, Boeing offers Bouelvard the 737-900.  
- OCtober 29:  Bouelvard purchases 100 acres of land in central MD outside of BWI for a new training center 
8. 1998
- Jan 15: First Boulevard Airline branded aircraft takes off from Baltimore.  
- Feb 10: Due to the merger of Boeing and McDonell Douglas, Boeing offers the Airline the newly rebranded 717, and MD-11
- June 14: First returns got back in and the need to increase aircraft frequency became a problem.  Comonality cited.  
- Jul 12: Mid Atlantic Charter switches to a Timeshare based aircraft model, purchasing King Airs, Citantion Jets, and the very first Gulstream. Boulevard Airlines Obsorbs the Flights however. 
- Aug 10:  Boulevard Airlines is in the red, MetroJet is profitable, hwoever US Airways notes it is not profitable enough and they are losing money.  Their finacial advisors see the company has entered into a growth trap.  
- Aug 21: Bouelvard enters into a new growth strategy that will hopefully This means moving into regional, long distance and itnernational travel.  Fuel eefecency was also cited.  We also noticed something interesting.  We were replacing US Airways on a number of routes.  It was decided to seek external information.
- October 1: Boulevard Buys an addtional 1000 acres in MD, and finally starts construction on a Hanover, MD based training faciilty.  Also, they cited that their current Severna Park HEadquarters was far too small THeys tarted looking for new real estate. 
- October 15: after breaking down in a buisness park in Windsor Mill, MD, they opted to buy two abandoned Warehosues and some empty land. The idea would be to build a full operational and modern operations facility.  
- November 12: The City of blatimore offered to give Mid-Atlatnic Charter (Reall Boulevard Airlines) free rent in the closing Montongomery Ward Building they were looking to get people into.  This would become the home of Mid-atlantic Charter.
- December 5: The companies problems were determined.  We littrally ahd everything duplicated.  We were runnign two airliens at the same time and only one was profitable, while the other delt with the debt.  
- December 10: After talking with the reps at New Air, we opted to try again to negotiate with Airbus.
9. 1999
- Janurary 15: Airbus pushes back the meeting for unknown reasons. 
- Feb 14: Bouelvard starts equipping aircraft with GPS tracking . It also equiped all aircraft with ACARS.  Intially this system and overhaul was epxensive, nearly costing $500 a flight.  However it streamlined cocpit operations. 
- March 7:  THe Reps at New Air break off contact.  
- March 8: American Airliens offloads their order of 50 ATR-500s and another 20 CRJ-700s.  They are aquired a 70% discount.  
- March 30: The company is invited to France again however this comes with a arning from US Airways about the Airbus as they continue to have fleet integration problems with the aircraft.  
- April 1: Bouelvard airlines enters a tentative agreement for the A320, however number of aircraft still needs to be worked out.
- April 15: New Air Changes its name to JetBlue, and they annouce their order of A320s.
- April 30: Boeing contacts Bouelvard offering to reduce cost and offers a new aircraft package at a quarter of what it would cost if the airline reject the Airbus order. 
- March 5: Airbus rejects Bouelvard, stating the DTIs are too high and proper finacing is not secured. 
- March 12: WIthout Telling Boeing, Boulevard accepts the Boeing deal to lease: 20 737-800s unti the 900's are ready, 20 757s, 10 767, and 10 747s.  The 777 was not added due to lack of any availiblity.  Many of these are designed to clear out orders taht are already on the books while the 737 comes from their own lessor.  
- November 18: The original BLVD investor's CEO dies.  The company is Aquired.  They give the company 2 years to be profitable or pay off all debts. 
10. 2000 
Jan 15: Finally Bouelvard starts reciving new Aircraft Although AA offers them some of its old MD-80s which the company accepts to aid in the trasition and allows for the 727s to start being retired. 
NO DATE: Bouelvard also becomes an international airline.
October 5: The new Training facility is complete.
October 10:  The airline entered into negotiations for its first in hosue MX facility in baltimore to lower MX cost. 
11. 2001
- Jan 1: The Airline's Investor filled appers in NYC to have the company shut down, with th intent to liquidated.  Bouelvard fileld papers 15 minutes earlier and filed papers to keep the airline opened, and opted to file a lawsuite as well in MD. The hope was to stall the NYC proceedings.  
- Jan 10: The ploy worked. The court proceedings would be held in MD. This also helped the airline secure a loan from the state to maintain operations and a Federal loan a well, but both were short term operating capital.  
- Feb: The first flight to Narita was an intial failure. Storms in CA kept the aircraft from flying otu of LAX, but an ANA aircraft timed out, so the CEO of a major Japanese investment firm took Boulevard since he ahd to get home.  Due to the delay, the plane was diverted to HND.  He liked the routing and was aware of their finacial issue. It was discovered the invemetn firm also was heavily invested in Jet Blue, having the signle largest stake in the company. They ahd also informed Airbus not to work with them. 
- March: The Investment team was looking for more foreign investment so contacted the staff.  After one month of showing him that the company was finally moving in the black (but they couldn't buy a new pin or they'd be back in the red) he opted to pay off the companies debt and invest the money the company needed to expand the fleet introduce,better commonality and elimitante the un prfitable Frankin fleet.  The codnition being if succesfull they would have have to creat a more profitable operation within Japan.  The agreemtn was signed on the 31st.  
- April 5: For the first time ever Bouelvard was profitable.  To better compelte with Jet Blue they started an upgrade program and reordered the itniors of all new aircraft.  They would have all leather interiors, seat back entertainment, and Stalight TV.  They opted for a csutom solution but a offshoot of Westing House made in MD.  The sysetem wsas far lighter and allowed for far more options.  The system would be in every aircraft from the ATR up to the 747. The airline now wanted to opt for a period of respcible expansion
- June 5: The new operations center come online. It has real time trackign of all aircraft.  
- October 1: The airline celebrated its first profitable fiscal year even with Metrojet, however US Airways renegotiated Metrojet, putting it almost on a month to month system.  
- Sepetmber 11:
   a. At around 8:40: Bouelvard airlines get's a call from the FAA notifying it that several airlines are missing.  Ops starts a head count of all aircraft
   b. 8:50:People in the lounge notify ops and managment of an aircraft crasing into the WTC.  
   c. 8:55: The head count is complete/ FAA notifies the airline to get itno positive contact with all aircraft. Company decides to reheck and ask for comfirmation of details, a protocal for hijacking. 
   d. 9:00: Bouelvard calls up the FAA and NORAD and tells them positive contact with all aircraft and all are in control. Both NORAD and FAA seem to think it i a 1 off, but most airlines have only contact a hand full of their aircraft. 
   e. 9:03: WTC South Tower Hit.
   f. 9:10: Without authorization from the FAA or NORAD, BLDV owner orders all aircraft onto the ground immediatly and suspends all deaprtures.  They then call the ATC and order them to cancel all flight plans.  OPS tems send out commands for all aircraf to proceed to the enarest commercial airport. 
   g. 9:20: First aircraft start landing in the fleet.
   h. 9:26: All departures banned in the US by FAA
   i: 9:37:  DCA crews report explosion from Pentegon.  A hit is confirmed 5 minutes latter. 
   g. 9:40: BLVD gates at  certain airports, like BWI fill up, and crews are rodered to juggel aircraft at teh gates. DCA flights rerouted to BWI or Richmond. 
   h. 9:45: US Airspace Closes
   i. 9:50: International flihghts inbound to the US from Europe are sent to Iceland or Toronto. Flights from Ascia are order to turn back and land in japan. 
   j. 10:30: 100% of the Domestic fleet is on the ground. 
   k. 11:15: 100% of the fleet world wide is on the ground. 
   l. 3:00PM: Busses and cars are auhorized for crew, and about 40% of the fleet is abandoned at out stations or non-desitnations, with the intent to rehome all crew who are located domestically within 24 hours. 
   m. 1100PM: Airline ops center goes to reduced staffing and the airlines is officially closed. 
- September 14: "Reoppening"  BLVD used Mid Atlantic Charters to fly crew back the stranded aircraft and stared flying some back to hubs.  We opted to jsut do a full reset.  Passengers levels were nearl 70% intially.
- September 16: Flights to desitnations were restarted, but Load factors were at 50%, so we opted to do a phased restart, and rollign furloughs, a plan that was devised during the shut down, this was actually fully devised during the fear of brnkrupcy.  The airline was dropped to 60% intially with crews flying around 60% of their normal schedule.  No new aircraft orders were canceld, instead opted to park them in the desert.  DCA remained closed. 
- September 23: USAir cuts Metrojet, although operations are allowed to continue.
- October 4: DCA reopens, however due to slot restrictions, and prioritztation of the larger carriers, Metrojet and Boulevard are not allowed to fly itnially.  Not ATRs were allowed nor CRJs.
- Dec 1: Metrojet contract was pulled, in arbitration, Bouelvard Airlines was paid out $10 million for the end of the contract with teh rest of the value coming from the Metrojet gates, aircraft (the 737-200) and equipment.  USAirways filed for bankrupcy.  That started a dangerous plan.  The airline would try to cannibalize ride share by feeding off the death of the legacy carriers. 
- Dec 15: The 737-200s were parked, replaced as 717s and 737-900s came online. Bouelvard gave US Airu its ten million back for 10% of their network and their A320 and A340 order