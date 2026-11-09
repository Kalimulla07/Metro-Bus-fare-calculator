/**
 * Metro & Bus Fare Calculator - Main Frontend JavaScript
 * Connects UI with Spring Boot REST API
 */

// API Base URL (relative path works automatically with Spring Boot static resources)
const API_BASE = "";

// State caches
let allStations = [];
let allRoutes = [];
let demandCache = null;

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

    // Auto-update distance in form when source or destination changes
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
    // Update active nav button
    document.querySelectorAll(".nav-btn").forEach(btn => {
        if (btn.getAttribute("data-tab") === tabId) {
            btn.classList.add("active");
        } else {
            btn.classList.remove("active");
        }
    });

    // Update active tab pane
    document.querySelectorAll(".tab-pane").forEach(pane => {
        if (pane.id === tabId) {
            pane.classList.add("active");
        } else {
            pane.classList.remove("active");
        }
    });

    // Re-render chart if dynamic pricing tab opened
    if (tabId === "tab-pricing" && demandCache) {
        renderDemandChart(demandCache.hourlyData, demandCache.peakThreshold);
    }
}

// ================= Load Stations & Populate Selects =================
async function initStationDropdowns() {
    try {
        const response = await fetch(`${API_BASE}/api/routes/stations`);
        if (!response.ok) throw new Error("Failed to load transit stations");

        allStations = await response.json();

        // Populate calculator dropdowns
        const sourceSelect = document.getElementById("sourceStation");
        const destSelect = document.getElementById("destinationStation");
        const routeSrc = document.getElementById("routeSource");
        const routeDst = document.getElementById("routeDest");

        sourceSelect.innerHTML = '<option value="">-- Select Source Station --</option>';
        destSelect.innerHTML = '<option value="">-- Select Destination Station --</option>';
        routeSrc.innerHTML = "";
        routeDst.innerHTML = "";

        allStations.forEach(st => {
            const opt1 = new Option(`${st.name} (Zone ${st.zone})`, st.name);
            const opt2 = new Option(`${st.name} (Zone ${st.zone})`, st.name);
            const opt3 = new Option(`${st.name} (Zone ${st.zone})`, st.name);
            const opt4 = new Option(`${st.name} (Zone ${st.zone})`, st.name);

            sourceSelect.add(opt1);
            destSelect.add(opt2);
            routeSrc.add(opt3);
            routeDst.add(opt4);
        });

        // Set default selections for a great initial demo
        if (sourceSelect.options.length > 1) sourceSelect.selectedIndex = 1; // Central Station
        if (destSelect.options.length > 5) destSelect.selectedIndex = 5;     // Tech Hub
        if (routeSrc.options.length > 1) routeSrc.selectedIndex = 1;
        if (routeDst.options.length > 5) routeDst.selectedIndex = 5;

        // Auto calculate distance for defaults
        handleStationSelectionChange();

        // Update station count on dashboard
        const stCountEl = document.getElementById("home-stations-count");
        if (stCountEl) stCountEl.innerText = `${allStations.length} Interconnected Stations`;

    } catch (err) {
        console.error("Error loading stations:", err);
        showToast("Could not load stations from backend. Is Spring Boot running?", "error");
    }
}

// When stations change, query shortest distance and populate distance input
async function handleStationSelectionChange() {
    const src = document.getElementById("sourceStation").value;
    const dst = document.getElementById("destinationStation").value;
    const mode = document.getElementById("transportType").value;
    const distInput = document.getElementById("distanceKm");

    if (!src || !dst || src === dst) {
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/api/routes/shortest-path?source=${encodeURIComponent(src)}&destination=${encodeURIComponent(dst)}&transportType=${mode}`);
        if (res.ok) {
            const data = await res.json();
            if (data.totalDistance > 0) {
                distInput.value = data.totalDistance;
            }
        }
    } catch (e) {
        console.warn("Could not auto-fetch route distance:", e);
    }
}

// ================= Dashboard Top 4 Cards =================
async function loadDashboardStats() {
    try {
        const response = await fetch(`${API_BASE}/api/fare/stats`);
        if (!response.ok) return;

        const stats = await response.json();
        document.getElementById("card-total-journeys").innerText = stats.totalJourneys;
        document.getElementById("card-avg-fare").innerText = `₹${stats.averageFare.toFixed(2)}`;
        document.getElementById("card-peak-fare").innerText = `₹${stats.peakHourFare.toFixed(2)}`;
        document.getElementById("card-offpeak-fare").innerText = `₹${stats.offPeakFare.toFixed(2)}`;
    } catch (err) {
        console.warn("Failed to load dashboard metrics:", err);
    }
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

        // Validation
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

        const payload = {
            transportType,
            sourceStation,
            destinationStation,
            distanceKm,
            passengerType,
            travelTime,
            strategyType
        };

        const submitBtn = document.getElementById("calc-submit-btn");
        submitBtn.disabled = true;
        submitBtn.innerText = "Calculating Fare...";

        try {
            const response = await fetch(`${API_BASE}/api/fare/calculate`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                const errData = await response.json();
                throw new Error(errData.error || "Fare calculation failed.");
            }

            const result = await response.json();
            displayFareResult(result);
            showToast("Fare calculated and recorded to MySQL history!", "success");

            // Refresh cards and history table
            loadDashboardStats();
            loadRecentCalculations();
            loadFareHistory();

        } catch (err) {
            showToast(err.message || "Failed to calculate fare.", "error");
        } finally {
            submitBtn.disabled = false;
            submitBtn.innerText = "Calculate Fare";
        }
    });
}

function displayFareResult(result) {
    document.getElementById("result-placeholder").classList.add("hidden");
    document.getElementById("result-details").classList.remove("hidden");

    document.getElementById("res-strategy-badge").innerText = result.strategyUsed;
    document.getElementById("res-final-fare").innerText = `₹${result.finalFare.toFixed(2)}`;
    document.getElementById("res-timestamp").innerText = `Calculated on: ${result.calculationTimestamp || "Just now"}`;

    document.getElementById("res-base-fare").innerText = `₹${result.baseFare.toFixed(2)}`;
    document.getElementById("res-distance-charge").innerText = `₹${result.distanceCharge.toFixed(2)}`;

    // Dynamic adjustment sign
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
    document.getElementById("res-record-id").innerText = result.historyId || "N/A";
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
        if (!response.ok) {
            const err = await response.json();
            throw new Error(err.error || "Route computation error");
        }

        const data = await response.json();

        // Render station path chain
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

        // Cache for quick transfer
        window.lastShortestDistance = data.totalDistance;
        window.lastRouteSrc = src;
        window.lastRouteDst = dst;
        window.lastRouteMode = mode;

    } catch (err) {
        showToast(err.message, "error");
    }
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
    try {
        const res = await fetch(`${API_BASE}/api/routes/all`);
        if (!res.ok) return;

        allRoutes = await res.json();
        const container = document.getElementById("routes-catalog-list");
        container.innerHTML = "";

        allRoutes.forEach(route => {
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
    } catch (e) {
        console.warn("Could not load route catalog:", e);
    }
}

// ================= Dynamic Pricing & Charting =================
async function loadDemandAnalytics() {
    try {
        const res = await fetch(`${API_BASE}/api/dynamic-pricing/demand-data`);
        if (!res.ok) return;

        demandCache = await res.json();

        // Update recommendation text
        const recEl = document.getElementById("dynamic-recommendation-text");
        if (recEl) recEl.innerText = demandCache.pricingRecommendationSummary;

        // Update home surge badge
        const surgeBadge = document.getElementById("live-demand-badge");
        const homeSurge = document.getElementById("home-surge-multiplier");

        const currentHour = new Date().getHours();
        const currentHourData = demandCache.hourlyData.find(d => d.hour === currentHour);

        if (currentHourData && currentHourData.isPeak) {
            if (surgeBadge) {
                surgeBadge.className = "badge red";
                surgeBadge.innerText = "Peak Demand Active";
            }
            if (homeSurge) {
                homeSurge.innerHTML = `<span class="text-warning">1.30x (+30% Surge Pricing)</span>`;
            }
        } else {
            if (surgeBadge) {
                surgeBadge.className = "badge green";
                surgeBadge.innerText = "Off-Peak (Normal/Saver)";
            }
            if (homeSurge) {
                homeSurge.innerHTML = `<span class="text-success">0.90x (10% Saver Rate)</span>`;
            }
        }

        renderDemandChart(demandCache.hourlyData, demandCache.peakThreshold);

    } catch (e) {
        console.warn("Could not load demand analytics:", e);
    }
}

function renderDemandChart(hourlyData, threshold) {
    const canvas = document.getElementById("demandChart");
    if (!canvas) return;

    const ctx = canvas.getContext("2d");
    const width = canvas.width;
    const height = canvas.height;

    // Clear canvas
    ctx.clearRect(0, 0, width, height);

    const padding = { top: 25, right: 30, bottom: 45, left: 60 };
    const chartW = width - padding.left - padding.right;
    const chartH = height - padding.top - padding.bottom;

    const maxPassengers = 12500;
    const numBars = hourlyData.length;
    const barWidth = (chartW / numBars) * 0.75;
    const gap = (chartW / numBars) * 0.25;

    // Draw horizontal grid lines
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

    // Draw threshold line (6,000 riders)
    const thresholdY = padding.top + chartH - (threshold / maxPassengers) * chartH;
    ctx.beginPath();
    ctx.strokeStyle = "#f59e0b";
    ctx.lineWidth = 2;
    ctx.setLineDash([6, 4]);
    ctx.moveTo(padding.left, thresholdY);
    ctx.lineTo(padding.left + chartW, thresholdY);
    ctx.stroke();
    ctx.setLineDash([]); // Reset dash

    // Draw bars
    ctx.textAlign = "center";
    hourlyData.forEach((item, index) => {
        const barH = (item.passengerCount / maxPassengers) * chartH;
        const x = padding.left + index * (barWidth + gap) + gap / 2;
        const y = padding.top + chartH - barH;

        // Choose color based on peak status
        ctx.fillStyle = item.isPeak ? "#dc2626" : "#2563eb";

        // Draw rounded top bar
        ctx.fillRect(x, y, barWidth, barH);

        // Hour Label every 2 hours
        if (index % 2 === 0) {
            ctx.fillStyle = "#64748b";
            ctx.fillText(item.timeSlot, x + barWidth / 2, padding.top + chartH + 16);
        }
    });
}

// ================= Fare History (MySQL) =================
async function loadFareHistory() {
    try {
        const res = await fetch(`${API_BASE}/api/fare/history`);
        if (!res.ok) return;

        const history = await res.json();
        const tbody = document.getElementById("history-table-body");
        tbody.innerHTML = "";

        if (history.length === 0) {
            tbody.innerHTML = `<tr><td colspan="11" class="text-center text-muted">No fare calculation history recorded yet.</td></tr>`;
            return;
        }

        history.forEach((row, idx) => {
            const tr = document.createElement("tr");

            // Format date nicely
            const dateStr = formatDateTime(row.calculationDate);

            tr.innerHTML = `
                <td><strong>#${row.id}</strong></td>
                <td><small>${dateStr}</small></td>
                <td><span class="badge ${row.transportType === 'METRO' ? 'blue' : 'orange'}">${row.transportType}</span></td>
                <td>${row.sourceStation}</td>
                <td>${row.destinationStation}</td>
                <td>${row.distanceKm} km</td>
                <td>${row.passengerType}</td>
                <td><span class="badge ${row.travelTime === 'Peak' ? 'red' : 'green'}">${row.travelTime}</span></td>
                <td><small>${row.strategyUsed}</small></td>
                <td><strong>₹${row.finalFare.toFixed(2)}</strong></td>
                <td>
                    <button class="btn btn-sm btn-danger" onclick="deleteHistoryItem(${row.id})">Delete</button>
                </td>
            `;
            tbody.appendChild(tr);
        });

    } catch (e) {
        console.warn("Could not load fare history:", e);
    }
}

async function loadRecentCalculations() {
    try {
        const res = await fetch(`${API_BASE}/api/fare/history`);
        if (!res.ok) return;

        const history = await res.json();
        const tbody = document.getElementById("home-recent-table-body");
        tbody.innerHTML = "";

        const recent = history.slice(0, 5); // first 5
        if (recent.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" class="text-center text-muted">No recent journeys recorded. Try calculating one!</td></tr>`;
            return;
        }

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
    } catch (e) {
        console.warn("Could not load recent table:", e);
    }
}

async function deleteHistoryItem(id) {
    if (!confirm(`Delete history record #${id}?`)) return;

    try {
        const res = await fetch(`${API_BASE}/api/fare/history/${id}`, { method: "DELETE" });
        if (res.ok) {
            showToast(`Record #${id} deleted.`, "success");
            loadFareHistory();
            loadRecentCalculations();
            loadDashboardStats();
        } else {
            showToast("Failed to delete record.", "error");
        }
    } catch (e) {
        showToast("Error communicating with backend.", "error");
    }
}

async function confirmClearHistory() {
    if (!confirm("Are you sure you want to clear ALL fare history records from MySQL? This action cannot be undone.")) return;

    try {
        const res = await fetch(`${API_BASE}/api/fare/history`, { method: "DELETE" });
        if (res.ok) {
            showToast("All history records cleared.", "success");
            loadFareHistory();
            loadRecentCalculations();
            loadDashboardStats();
        }
    } catch (e) {
        showToast("Error clearing history.", "error");
    }
}

// ================= Utilities =================
function showToast(message, type = "success") {
    const toast = document.getElementById("toast-message");
    if (!toast) return;

    toast.className = `toast ${type}`;
    toast.innerText = message;
    toast.classList.remove("hidden");

    setTimeout(() => {
        toast.classList.add("hidden");
    }, 4500);
}

function formatDateTime(dtStr) {
    if (!dtStr) return "";
    try {
        const dt = new Date(dtStr);
        return dt.toLocaleDateString("en-US", {
            month: "short",
            day: "numeric",
            hour: "2-digit",
            minute: "2-digit"
        });
    } catch (e) {
        return dtStr;
    }
}
