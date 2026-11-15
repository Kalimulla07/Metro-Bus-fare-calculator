/**
 * Metro & Bus Fare Calculator - Main Frontend JavaScript
 * Connects UI with Spring Boot REST API with offline/static fallback for GitHub Pages
 */

const API_BASE = "";

// State caches
let allStations = [];
let allRoutes = [];
let demandCache = null;

// Built-in station directory for offline / static hosting (e.g. GitHub Pages)
const DEFAULT_STATIONS = [
    { id: "ST01", name: "Central Station", zone: 1, line: "Red Line / Blue Line", transportType: "BOTH" },
    { id: "ST02", name: "City Center", zone: 1, line: "Red Line / Blue Line", transportType: "BOTH" },
    { id: "ST03", name: "Downtown", zone: 1, line: "Red Line / Green Line", transportType: "BOTH" },
    { id: "ST04", name: "University", zone: 2, line: "Red Line", transportType: "METRO" },
    { id: "ST05", name: "Tech Hub", zone: 2, line: "Red Line", transportType: "BOTH" },
    { id: "ST06", name: "Suburb North", zone: 3, line: "Red Line", transportType: "METRO" },
    { id: "ST07", name: "Airport Terminal", zone: 3, line: "Blue Line", transportType: "BOTH" },
    { id: "ST08", name: "West End", zone: 2, line: "Blue Line", transportType: "BOTH" },
    { id: "ST09", name: "Harbor Point", zone: 2, line: "Blue Line", transportType: "BOTH" },
    { id: "ST10", name: "East Gate", zone: 3, line: "Blue Line", transportType: "BOTH" },
    { id: "ST11", name: "Medical Center", zone: 2, line: "Green Line", transportType: "METRO" },
    { id: "ST12", name: "Sports Arena", zone: 2, line: "Green Line", transportType: "BOTH" },
    { id: "ST13", name: "South Park", zone: 3, line: "Green Line", transportType: "BOTH" }
];

const DEFAULT_ROUTES = [
    { routeName: "Metro Red Line (North-Central)", transportType: "METRO", color: "#dc2626", totalDistanceKm: 26.5, stationNames: ["Central Station", "City Center", "Downtown", "University", "Tech Hub", "Suburb North"] },
    { routeName: "Metro Blue Line (Airport-East)", transportType: "METRO", color: "#2563eb", totalDistanceKm: 31.0, stationNames: ["Airport Terminal", "West End", "City Center", "Central Station", "Harbor Point", "East Gate"] },
    { routeName: "Metro Green Line (Downtown-South)", transportType: "METRO", color: "#16a34a", totalDistanceKm: 21.0, stationNames: ["Downtown", "Medical Center", "Sports Arena", "South Park"] },
    { routeName: "Bus Route 101 (Airport Express)", transportType: "BUS", color: "#d97706", totalDistanceKm: 22.0, stationNames: ["Central Station", "West End", "Airport Terminal"] },
    { routeName: "Bus Route 202 (Crosstown Link)", transportType: "BUS", color: "#9333ea", totalDistanceKm: 24.5, stationNames: ["Tech Hub", "University", "Downtown", "Harbor Point"] },
    { routeName: "Bus Route 303 (East-South Connector)", transportType: "BUS", color: "#0891b2", totalDistanceKm: 18.0, stationNames: ["East Gate", "Sports Arena", "South Park"] }
];

// ================= DOM Ready Initialization =================
document.addEventListener("DOMContentLoaded", () => {
    initNavigation();
    initStationDropdowns();
    initCalculatorForm();
    loadDashboardStats();
    loadRecentCalculations();
    loadRoutesCatalog();
    loadDemandAnalytics();
    loadFareHistory();

    const sourceSelect = document.getElementById("sourceStation");
    const destSelect = document.getElementById("destinationStation");
    if (sourceSelect && destSelect) {
        sourceSelect.addEventListener("change", handleStationSelectionChange);
        destSelect.addEventListener("change", handleStationSelectionChange);
    }
});

// ================= Navigation Tabs =================
function initNavigation() {
    const navButtons = document.querySelectorAll(".nav-btn");
    navButtons.forEach(btn => {
        btn.addEventListener("click", () => {
            const targetTabId = btn.getAttribute("data-tab");
            switchTab(targetTabId);
        });
    });
}

function switchTab(tabId) {
    document.querySelectorAll(".nav-btn").forEach(btn => {
        btn.classList.toggle("active", btn.getAttribute("data-tab") === tabId);
    });

    document.querySelectorAll(".tab-pane").forEach(pane => {
        pane.classList.toggle("active", pane.id === tabId);
    });

    if (tabId === "tab-pricing" && demandCache) {
        renderDemandChart(demandCache.hourlyData, demandCache.peakThreshold);
    }
}

// ================= Load Stations & Populate Selects =================
async function initStationDropdowns() {
    try {
        const response = await fetch(`${API_BASE}/api/routes/stations`);
        if (response.ok) {
            allStations = await response.json();
        } else {
            allStations = DEFAULT_STATIONS;
        }
    } catch (err) {
        // Fallback for static GitHub Pages hosting
        allStations = DEFAULT_STATIONS;
    }

    populateStationSelects(allStations);
}

function populateStationSelects(stations) {
    const sourceSelect = document.getElementById("sourceStation");
    const destSelect = document.getElementById("destinationStation");
    const routeSrc = document.getElementById("routeSource");
    const routeDst = document.getElementById("routeDest");

    sourceSelect.innerHTML = '<option value="">-- Select Source Station --</option>';
    destSelect.innerHTML = '<option value="">-- Select Destination Station --</option>';
    routeSrc.innerHTML = "";
    routeDst.innerHTML = "";

    stations.forEach(st => {
        sourceSelect.add(new Option(`${st.name} (Zone ${st.zone})`, st.name));
        destSelect.add(new Option(`${st.name} (Zone ${st.zone})`, st.name));
        routeSrc.add(new Option(`${st.name} (Zone ${st.zone})`, st.name));
        routeDst.add(new Option(`${st.name} (Zone ${st.zone})`, st.name));
    });

    if (sourceSelect.options.length > 1) sourceSelect.selectedIndex = 1; // Central Station
    if (destSelect.options.length > 5) destSelect.selectedIndex = 5;     // Tech Hub
    if (routeSrc.options.length > 1) routeSrc.selectedIndex = 1;
    if (routeDst.options.length > 5) routeDst.selectedIndex = 5;

    handleStationSelectionChange();

    const stCountEl = document.getElementById("home-stations-count");
    if (stCountEl) stCountEl.innerText = `${stations.length} Interconnected Stations`;
}

// Auto-fill distance between stations
async function handleStationSelectionChange() {
    const src = document.getElementById("sourceStation").value;
    const dst = document.getElementById("destinationStation").value;
    const mode = document.getElementById("transportType").value;
    const distInput = document.getElementById("distanceKm");

    if (!src || !dst || src === dst) return;

    try {
        const res = await fetch(`${API_BASE}/api/routes/shortest-path?source=${encodeURIComponent(src)}&destination=${encodeURIComponent(dst)}&transportType=${mode}`);
        if (res.ok) {
            const data = await res.json();
            if (data.totalDistance > 0) {
                distInput.value = data.totalDistance;
                return;
            }
        }
    } catch (e) {
        // Fallback calculation for offline / static hosting
    }

    // Heuristic estimation based on station indices
    const idx1 = allStations.findIndex(s => s.name === src);
    const idx2 = allStations.findIndex(s => s.name === dst);
    const distanceEstimate = Math.max(2.5, Math.abs(idx1 - idx2) * 4.2);
    distInput.value = distanceEstimate.toFixed(1);
}

// ================= Dashboard Top 4 Cards =================
async function loadDashboardStats() {
    try {
        const response = await fetch(`${API_BASE}/api/fare/stats`);
        if (response.ok) {
            const stats = await response.json();
            document.getElementById("card-total-journeys").innerText = stats.totalJourneys;
            document.getElementById("card-avg-fare").innerText = `₹${stats.averageFare.toFixed(2)}`;
            document.getElementById("card-peak-fare").innerText = `₹${stats.peakHourFare.toFixed(2)}`;
            document.getElementById("card-offpeak-fare").innerText = `₹${stats.offPeakFare.toFixed(2)}`;
            return;
        }
    } catch (err) {}

    // Fallback metrics
    document.getElementById("card-total-journeys").innerText = "12";
    document.getElementById("card-avg-fare").innerText = "₹48.50";
    document.getElementById("card-peak-fare").innerText = "₹62.00";
    document.getElementById("card-offpeak-fare").innerText = "₹34.00";
}

// ================= Fare Calculator Submission =================
function initCalculatorForm() {
    const form = document.getElementById("fare-calculator-form");
    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const transportType = document.getElementById("transportType").value;
        const sourceStation = document.getElementById("sourceStation").value;
        const destinationStation = document.getElementById("destinationStation").value;
        const distanceKm = parseFloat(document.getElementById("distanceKm").value);
        const passengerType = document.getElementById("passengerType").value;
        const travelTime = document.getElementById("travelTime").value;
        const strategyType = document.getElementById("strategyType").value;

        if (!sourceStation || !destinationStation) {
            showToast("Please choose both source and destination stations.", "error");
            return;
        }

        if (sourceStation === destinationStation) {
            showToast("Source and destination stations cannot be identical.", "error");
            return;
        }

        if (isNaN(distanceKm) || distanceKm <= 0) {
            showToast("Please provide a valid distance in kilometers.", "error");
            return;
        }

        const payload = { transportType, sourceStation, destinationStation, distanceKm, passengerType, travelTime, strategyType };

        const submitBtn = document.getElementById("calc-submit-btn");
        submitBtn.disabled = true;
        submitBtn.innerText = "Calculating Fare...";

        try {
            const response = await fetch(`${API_BASE}/api/fare/calculate`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });

            if (response.ok) {
                const result = await response.json();
                displayFareResult(result);
                showToast("Fare calculated and recorded to MySQL history!", "success");
                loadDashboardStats();
                loadRecentCalculations();
                loadFareHistory();
                return;
            }
        } catch (err) {}

        // Fallback calculation for static / offline demo
        const result = calculateFareOffline(payload);
        displayFareResult(result);
        showToast("Fare calculated successfully!", "success");

        submitBtn.disabled = false;
        submitBtn.innerText = "Calculate Fare";
    });
}

// Offline fallback computation matching Java Strategy logic
function calculateFareOffline(req) {
    const isMetro = req.transportType === "METRO";
    const base = isMetro ? 20.0 : 15.0;
    const rate = isMetro ? 2.0 : 1.2;
    const distCharge = Math.round(req.distanceKm * rate * 100) / 100;

    let dynamicAdj = 0.0;
    if (req.strategyType === "PEAK_OFF_PEAK") {
        dynamicAdj = req.travelTime === "Peak" ? Math.round((base + distCharge) * 0.30 * 100) / 100 : Math.round(-(base + distCharge) * 0.10 * 100) / 100;
    } else if (req.travelTime === "Peak") {
        dynamicAdj = Math.round((base + distCharge) * 0.10 * 100) / 100;
    }

    const subtotal = Math.max(10.0, base + distCharge + dynamicAdj);
    let discountRate = 0.0;
    if (req.passengerType === "Student") discountRate = 0.25;
    if (req.passengerType === "Senior Citizen") discountRate = 0.40;

    const discount = Math.round(subtotal * discountRate * 100) / 100;
    const finalFare = Math.round((subtotal - discount) * 100) / 100;

    return {
        baseFare: base,
        distanceCharge: distCharge,
        dynamicAdjustment: dynamicAdj,
        discountAmount: discount,
        finalFare: finalFare,
        strategyUsed: req.strategyType === "ZONE" ? "Zone-Based Strategy" : (req.strategyType === "PEAK_OFF_PEAK" ? "Peak/Off-Peak Dynamic Strategy" : "Distance-Based Strategy"),
        breakdownDescription: `Base (₹${base.toFixed(2)}) + Distance (₹${distCharge.toFixed(2)}) ${dynamicAdj >= 0 ? '+ Surge' : '- Saver'} (₹${Math.abs(dynamicAdj).toFixed(2)}) - Discount (₹${discount.toFixed(2)}) = Final Fare ₹${finalFare.toFixed(2)}`,
        historyId: Math.floor(Math.random() * 900) + 100,
        calculationTimestamp: new Date().toLocaleTimeString()
    };
}

function displayFareResult(result) {
    document.getElementById("result-placeholder").classList.add("hidden");
    document.getElementById("result-details").classList.remove("hidden");

    document.getElementById("res-strategy-badge").innerText = result.strategyUsed;
    document.getElementById("res-final-fare").innerText = `₹${result.finalFare.toFixed(2)}`;
    document.getElementById("res-timestamp").innerText = `Calculated on: ${result.calculationTimestamp || "Just now"}`;

    document.getElementById("res-base-fare").innerText = `₹${result.baseFare.toFixed(2)}`;
    document.getElementById("res-distance-charge").innerText = `₹${result.distanceCharge.toFixed(2)}`;

    const adjEl = document.getElementById("res-dynamic-adjustment");
    if (result.dynamicAdjustment > 0) {
        adjEl.innerText = `+₹${result.dynamicAdjustment.toFixed(2)} (Peak Surge)`;
        adjEl.className = "text-warning";
    } else if (result.dynamicAdjustment < 0) {
        adjEl.innerText = `-₹${Math.abs(result.dynamicAdjustment).toFixed(2)} (Off-Peak Saver)`;
        adjEl.className = "text-success";
    } else {
        adjEl.innerText = "₹0.00";
        adjEl.className = "text-muted";
    }

    document.getElementById("res-discount").innerText = `-₹${result.discountAmount.toFixed(2)}`;
    document.getElementById("res-breakdown-desc").innerText = result.breakdownDescription;
    document.getElementById("res-record-id").innerText = result.historyId || "Demo";
}

// ================= Route Optimization (Dijkstra) =================
async function calculateShortestRoute() {
    const src = document.getElementById("routeSource").value;
    const dst = document.getElementById("routeDest").value;
    const mode = document.getElementById("routeMode").value;

    if (!src || !dst) {
        showToast("Please choose both origin and destination stations.", "error");
        return;
    }

    if (src === dst) {
        showToast("Origin and destination are the same station.", "error");
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/api/routes/shortest-path?source=${encodeURIComponent(src)}&destination=${encodeURIComponent(dst)}&transportType=${mode}`);
        if (response.ok) {
            const data = await response.json();
            renderRouteResult(data);
            return;
        }
    } catch (err) {}

    // Fallback route result for static hosting
    const data = {
        source: src,
        destination: dst,
        transportType: mode,
        route: [src, "City Center", "Downtown", dst],
        totalDistance: 16.5,
        estimatedFare: 53.00,
        estimatedMinutes: 28,
        totalStops: 4
    };
    renderRouteResult(data);
}

function renderRouteResult(data) {
    const chainEl = document.getElementById("route-chain");
    chainEl.innerHTML = "";

    data.route.forEach((station, idx) => {
        const pill = document.createElement("span");
        pill.className = "station-pill";
        pill.innerText = station;
        chainEl.appendChild(pill);

        if (idx < data.route.length - 1) {
            const arrow = document.createElement("span");
            arrow.className = "chain-arrow";
            arrow.innerHTML = "&rarr;";
            chainEl.appendChild(arrow);
        }
    });

    document.getElementById("route-total-km").innerText = `${data.totalDistance} km`;
    document.getElementById("route-total-time").innerText = `~${data.estimatedMinutes} mins`;
    document.getElementById("route-est-fare").innerText = `₹${data.estimatedFare.toFixed(2)}`;
    document.getElementById("route-total-stops").innerText = `${data.totalStops} stations`;

    document.getElementById("route-output-box").classList.remove("hidden");

    window.lastShortestDistance = data.totalDistance;
    window.lastRouteSrc = data.source;
    window.lastRouteDst = data.destination;
    window.lastRouteMode = data.transportType;
}

function transferRouteToCalculator() {
    if (window.lastShortestDistance) {
        document.getElementById("sourceStation").value = window.lastRouteSrc;
        document.getElementById("destinationStation").value = window.lastRouteDst;
        document.getElementById("distanceKm").value = window.lastShortestDistance;
        document.getElementById("transportType").value = window.lastRouteMode || "METRO";

        switchTab("tab-calculator");
        showToast(`Route applied: ${window.lastRouteSrc} to ${window.lastRouteDst} (${window.lastShortestDistance} km)`, "success");
    }
}

// ================= Available Routes Catalog =================
async function loadRoutesCatalog() {
    let routes = DEFAULT_ROUTES;
    try {
        const res = await fetch(`${API_BASE}/api/routes/all`);
        if (res.ok) routes = await res.json();
    } catch (e) {}

    const container = document.getElementById("routes-catalog-list");
    container.innerHTML = "";

    routes.forEach(route => {
        const card = document.createElement("div");
        card.className = "route-line-card";
        card.style.borderLeftColor = route.color;

        card.innerHTML = `
            <div style="display:flex; justify-content:space-between; align-items:center;">
                <h4>${route.routeName}</h4>
                <span class="badge ${route.transportType === 'METRO' ? 'blue' : 'orange'}">${route.transportType} &bull; ${route.totalDistanceKm} km</span>
            </div>
            <p><strong>Stations:</strong> ${route.stationNames.join(" &bull; ")}</p>
        `;
        container.appendChild(card);
    });
}

// ================= Dynamic Pricing & Charting =================
async function loadDemandAnalytics() {
    try {
        const res = await fetch(`${API_BASE}/api/dynamic-pricing/demand-data`);
        if (res.ok) {
            demandCache = await res.json();
            applyDemandData(demandCache);
            return;
        }
    } catch (e) {}

    // Fallback demand data
    const hourlyList = [];
    const counts = [650, 400, 350, 500, 1100, 2800, 5200, 7400, 9600, 10400, 8100, 4600, 4200, 4100, 4800, 5400, 6800, 9900, 11500, 8900, 5300, 3800, 2400, 1300];
    for (let h = 0; h < 24; h++) {
        hourlyList.push({
            timeSlot: `${h < 10 ? '0' : ''}${h}:00`,
            hour: h,
            passengerCount: counts[h],
            isPeak: counts[h] >= 6000,
            surgeMultiplier: counts[h] >= 6000 ? 1.30 : 0.90
        });
    }

    demandCache = {
        peakThreshold: 6000,
        hourlyData: hourlyList,
        pricingRecommendationSummary: "Shift discretionary trips to 11:00 AM - 04:00 PM or after 08:00 PM to save up to 40% on overall transit fares and help alleviate peak congestion."
    };
    applyDemandData(demandCache);
}

function applyDemandData(data) {
    const recEl = document.getElementById("dynamic-recommendation-text");
    if (recEl) recEl.innerText = data.pricingRecommendationSummary;

    const surgeBadge = document.getElementById("live-demand-badge");
    const homeSurge = document.getElementById("home-surge-multiplier");

    const currentHour = new Date().getHours();
    const currentHourData = data.hourlyData.find(d => d.hour === currentHour);

    if (currentHourData && currentHourData.isPeak) {
        if (surgeBadge) { surgeBadge.className = "badge red"; surgeBadge.innerText = "Peak Demand Active"; }
        if (homeSurge) homeSurge.innerHTML = `<span class="text-warning">1.30x (+30% Surge Pricing)</span>`;
    } else {
        if (surgeBadge) { surgeBadge.className = "badge green"; surgeBadge.innerText = "Off-Peak (Normal/Saver)"; }
        if (homeSurge) homeSurge.innerHTML = `<span class="text-success">0.90x (10% Saver Rate)</span>`;
    }

    renderDemandChart(data.hourlyData, data.peakThreshold);
}

function renderDemandChart(hourlyData, threshold) {
    const canvas = document.getElementById("demandChart");
    if (!canvas) return;

    const ctx = canvas.getContext("2d");
    const width = canvas.width;
    const height = canvas.height;

    ctx.clearRect(0, 0, width, height);

    const padding = { top: 25, right: 30, bottom: 45, left: 60 };
    const chartW = width - padding.left - padding.right;
    const chartH = height - padding.top - padding.bottom;

    const maxPassengers = 12500;
    const numBars = hourlyData.length;
    const barWidth = (chartW / numBars) * 0.75;
    const gap = (chartW / numBars) * 0.25;

    ctx.strokeStyle = "#e2e8f0";
    ctx.lineWidth = 1;
    ctx.fillStyle = "#94a3b8";
    ctx.font = "10px -apple-system, sans-serif";
    ctx.textAlign = "right";

    const steps = 4;
    for (let i = 0; i <= steps; i++) {
        const val = (maxPassengers / steps) * i;
        const y = padding.top + chartH - (i / steps) * chartH;
        ctx.beginPath();
        ctx.moveTo(padding.left, y);
        ctx.lineTo(padding.left + chartW, y);
        ctx.stroke();
        ctx.fillText(`${val.toLocaleString()}`, padding.left - 8, y + 3);
    }

    const thresholdY = padding.top + chartH - (threshold / maxPassengers) * chartH;
    ctx.beginPath();
    ctx.strokeStyle = "#f59e0b";
    ctx.lineWidth = 2;
    ctx.setLineDash([6, 4]);
    ctx.moveTo(padding.left, thresholdY);
    ctx.lineTo(padding.left + chartW, thresholdY);
    ctx.stroke();
    ctx.setLineDash([]);

    ctx.textAlign = "center";
    hourlyData.forEach((item, index) => {
        const barH = (item.passengerCount / maxPassengers) * chartH;
        const x = padding.left + index * (barWidth + gap) + gap / 2;
        const y = padding.top + chartH - barH;
        ctx.fillStyle = item.isPeak ? "#dc2626" : "#2563eb";
        ctx.fillRect(x, y, barWidth, barH);

        if (index % 2 === 0) {
            ctx.fillStyle = "#64748b";
            ctx.fillText(item.timeSlot, x + barWidth / 2, padding.top + chartH + 16);
        }
    });
}

// ================= Fare History =================
async function loadFareHistory() {
    try {
        const res = await fetch(`${API_BASE}/api/fare/history`);
        if (res.ok) {
            const history = await res.json();
            renderHistoryTable(history);
            return;
        }
    } catch (e) {}

    // Fallback sample history
    const sampleHistory = [
        { id: 1, calculationDate: new Date(), transportType: "METRO", sourceStation: "Central Station", destinationStation: "Tech Hub", distanceKm: 18.5, passengerType: "Normal", travelTime: "Peak", strategyUsed: "Distance-Based", finalFare: 68.40 },
        { id: 2, calculationDate: new Date(), transportType: "BUS", sourceStation: "Airport Terminal", destinationStation: "City Center", distanceKm: 25.0, passengerType: "Student", travelTime: "Off-Peak", strategyUsed: "Zone-Based", finalFare: 33.75 }
    ];
    renderHistoryTable(sampleHistory);
}

function renderHistoryTable(history) {
    const tbody = document.getElementById("history-table-body");
    tbody.innerHTML = "";

    if (history.length === 0) {
        tbody.innerHTML = `<tr><td colspan="11" class="text-center text-muted">No fare history recorded yet.</td></tr>`;
        return;
    }

    history.forEach(row => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td><strong>#${row.id}</strong></td>
            <td><small>${formatDateTime(row.calculationDate)}</small></td>
            <td><span class="badge ${row.transportType === 'METRO' ? 'blue' : 'orange'}">${row.transportType}</span></td>
            <td>${row.sourceStation}</td>
            <td>${row.destinationStation}</td>
            <td>${row.distanceKm} km</td>
            <td>${row.passengerType}</td>
            <td><span class="badge ${row.travelTime === 'Peak' ? 'red' : 'green'}">${row.travelTime}</span></td>
            <td><small>${row.strategyUsed}</small></td>
            <td><strong>₹${row.finalFare.toFixed(2)}</strong></td>
            <td><button class="btn btn-sm btn-danger" onclick="deleteHistoryItem(${row.id})">Delete</button></td>
        `;
        tbody.appendChild(tr);
    });
}

async function loadRecentCalculations() {
    try {
        const res = await fetch(`${API_BASE}/api/fare/history`);
        if (res.ok) {
            const history = await res.json();
            renderRecentTable(history.slice(0, 5));
            return;
        }
    } catch (e) {}

    renderRecentTable([
        { calculationDate: new Date(), transportType: "METRO", sourceStation: "Central Station", destinationStation: "Tech Hub", distanceKm: 18.5, passengerType: "Normal", travelTime: "Peak", finalFare: 68.40 },
        { calculationDate: new Date(), transportType: "BUS", sourceStation: "Airport Terminal", destinationStation: "City Center", distanceKm: 25.0, passengerType: "Student", travelTime: "Off-Peak", finalFare: 33.75 }
    ]);
}

function renderRecentTable(recent) {
    const tbody = document.getElementById("home-recent-table-body");
    tbody.innerHTML = "";

    recent.forEach(row => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td><small>${formatDateTime(row.calculationDate)}</small></td>
            <td><span class="badge ${row.transportType === 'METRO' ? 'blue' : 'orange'}">${row.transportType}</span></td>
            <td>${row.sourceStation} &rarr; ${row.destinationStation}</td>
            <td>${row.distanceKm} km</td>
            <td>${row.passengerType}</td>
            <td><span class="badge ${row.travelTime === 'Peak' ? 'red' : 'green'}">${row.travelTime}</span></td>
            <td><strong>₹${row.finalFare.toFixed(2)}</strong></td>
        `;
        tbody.appendChild(tr);
    });
}

async function deleteHistoryItem(id) {
    try {
        const res = await fetch(`${API_BASE}/api/fare/history/${id}`, { method: "DELETE" });
        if (res.ok) showToast(`Record #${id} deleted.`, "success");
    } catch (e) {}
    loadFareHistory();
    loadDashboardStats();
}

function showToast(message, type = "success") {
    const toast = document.getElementById("toast-message");
    if (!toast) return;
    toast.className = `toast ${type}`;
    toast.innerText = message;
    toast.classList.remove("hidden");
    setTimeout(() => toast.classList.add("hidden"), 4500);
}

function formatDateTime(dtStr) {
    if (!dtStr) return "";
    try {
        const dt = new Date(dtStr);
        return dt.toLocaleDateString("en-US", { month: "short", day: "numeric", hour: "2-digit", minute: "2-digit" });
    } catch (e) {
        return dtStr;
    }
}
