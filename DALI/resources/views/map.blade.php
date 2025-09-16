<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <title>Traffic Agent Map</title>
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
    <style>
        #map {
            height: 100vh;
        }

        .marker-R {
            background-color: red;
        }

        .marker-G {
            background-color: green;
        }

        .marker-B {
            background-color: blue;
        }

        .marker-Y {
            background-color: yellow;
        }
    </style>
</head>

<body>
    <div id="map"></div>

    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
    <script>
        const map = L.map('map').setView([0, 0], 2);
        const tiles = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; OpenStreetMap contributors'
        }).addTo(map);

        let markers = {};

        function fetchAgents() {
            fetch('/api/agents')
                .then(res => res.json())
                .then(data => {
                    data.forEach(agent => {
                        const key = `agent-${agent.id}`;
                        const icon = L.divIcon({
                            className: `marker-${agent.state}`,
                            html: `<div style="width:12px; height:12px; border-radius:50%; background:${getColor(agent.state)};"></div>`,
                        });

                        if (markers[key]) {
                            markers[key].setLatLng([agent.latitude, agent.longitude]);
                            markers[key].setIcon(icon);
                        } else {
                            const marker = L.marker([agent.latitude, agent.longitude], { icon })
                                .bindPopup(`<strong>${agent.name || 'Unnamed'} (${agent.type})</strong><br>State: ${agent.state}`)
                                .addTo(map);
                            markers[key] = marker;
                        }
                    });
                });
        }

        function getColor(state) {
            return {
                R: 'red',
                G: 'green',
                B: 'blue',
                Y: 'yellow'
            }[state] || 'gray';
        }

        fetchAgents();
        setInterval(fetchAgents, 3000); // every 3 seconds
    </script>
</body>

</html>