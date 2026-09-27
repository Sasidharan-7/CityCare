/**
 * CityCare - Map Module
 * 
 * Uses Leaflet.js + OpenStreetMap for location services.
 * Handles: GPS location, interactive markers, complaint map.
 */

const MapModule = (() => {
  let map = null;
  let marker = null;
  let complaintMarkers = [];

  // Default center (India) – used as fallback if GPS is denied
  const DEFAULT_CENTER = [20.5937, 78.9629];
  const DEFAULT_ZOOM = 5;
  const LOCATED_ZOOM = 16;

  /**
   * Initialize a Leaflet map in the given container.
   * @param {string} containerId - DOM id of the map container
   * @param {object} options - { center, zoom, onClick }
   * @returns {L.Map}
   */
  function initMap(containerId, options = {}) {
    const center = options.center || DEFAULT_CENTER;
    const zoom = options.zoom || DEFAULT_ZOOM;

    map = L.map(containerId).setView(center, zoom);

    // OpenStreetMap tile layer
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
      maxZoom: 19,
    }).addTo(map);

    // Click handler for manual location selection
    if (options.onClick) {
      map.on('click', (e) => {
        options.onClick(e.latlng.lat, e.latlng.lng);
      });
    }

    return map;
  }

  /**
   * Set or move a single marker on the map.
   */
  function setMarker(lat, lng, popupText) {
    if (!map) return;

    if (marker) {
      marker.setLatLng([lat, lng]);
    } else {
      marker = L.marker([lat, lng]).addTo(map);
    }

    if (popupText) {
      marker.bindPopup(popupText);
    }

    map.setView([lat, lng], LOCATED_ZOOM);
  }

  /**
   * Remove the current marker.
   */
  function removeMarker() {
    if (marker && map) {
      map.removeLayer(marker);
      marker = null;
    }
  }

  /**
   * Get user's current GPS location.
   * @returns {Promise<{lat: number, lng: number}>}
   */
  function getCurrentLocation() {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        reject(new Error('Geolocation is not supported by your browser.'));
        return;
      }

      navigator.geolocation.getCurrentPosition(
        (position) => {
          resolve({
            lat: position.coords.latitude,
            lng: position.coords.longitude,
          });
        },
        (error) => {
          switch (error.code) {
            case error.PERMISSION_DENIED:
              reject(new Error('Location access was denied. Please select the issue location manually on the map.'));
              break;
            case error.POSITION_UNAVAILABLE:
              reject(new Error('Location information is unavailable.'));
              break;
            case error.TIMEOUT:
              reject(new Error('Location request timed out.'));
              break;
            default:
              reject(new Error('An unknown error occurred while getting location.'));
          }
        },
        {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 60000,
        }
      );
    });
  }

  /**
   * Create custom colored markers based on priority/status.
   */
  function getMarkerIcon(priority) {
    const colors = {
      CRITICAL: '#dc2626',
      HIGH: '#ea580c',
      MEDIUM: '#d97706',
      LOW: '#16a34a',
    };
    const color = colors[priority] || '#2563eb';

    return L.divIcon({
      className: 'custom-marker',
      html: `<div style="
        width: 24px; height: 24px; 
        background: ${color}; 
        border: 3px solid white; 
        border-radius: 50%; 
        box-shadow: 0 2px 6px rgba(0,0,0,0.3);
      "></div>`,
      iconSize: [24, 24],
      iconAnchor: [12, 12],
      popupAnchor: [0, -14],
    });
  }

  /**
   * Add multiple complaint markers to the map.
   * @param {Array} complaints - array of complaint objects
   */
  function addComplaintMarkers(complaints) {
    clearComplaintMarkers();

    complaints.forEach((c) => {
      if (!c.latitude || !c.longitude) return;

      const icon = getMarkerIcon(c.priority);
      const m = L.marker([c.latitude, c.longitude], { icon }).addTo(map);

      m.bindPopup(`
        <div class="map-popup-content">
          <strong>#${c.complaintId || c.id}</strong><br>
          <strong>${App.categoryLabel(c.category)}</strong><br>
          ${c.description ? c.description.substring(0, 80) + '...' : ''}<br>
          <div class="popup-meta">
            <span class="badge ${App.statusBadge(c.status)}">${App.statusLabel(c.status)}</span>
            <span class="badge ${App.priorityBadge(c.priority)}">${c.priority}</span>
          </div>
        </div>
      `);

      complaintMarkers.push(m);
    });

    // Fit bounds if there are markers
    if (complaintMarkers.length > 0) {
      const group = L.featureGroup(complaintMarkers);
      map.fitBounds(group.getBounds().pad(0.1));
    }
  }

  /**
   * Clear all complaint markers from the map.
   */
  function clearComplaintMarkers() {
    complaintMarkers.forEach((m) => {
      if (map) map.removeLayer(m);
    });
    complaintMarkers = [];
  }

  /**
   * Get the current map instance.
   */
  function getMap() {
    return map;
  }

  /**
   * Invalidate map size (needed after container becomes visible).
   */
  function invalidateSize() {
    if (map) {
      setTimeout(() => map.invalidateSize(), 100);
    }
  }

  return {
    initMap,
    setMarker,
    removeMarker,
    getCurrentLocation,
    getMarkerIcon,
    addComplaintMarkers,
    clearComplaintMarkers,
    getMap,
    invalidateSize,
    DEFAULT_CENTER,
    DEFAULT_ZOOM,
    LOCATED_ZOOM,
  };
})();
