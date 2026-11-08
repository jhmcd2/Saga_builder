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
