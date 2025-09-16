<div>
    <h2 class="text-xl font-bold mb-4 flex justify-start items-center">
        <img src="{{Vite::asset('resources/assets/s-dali.png')}}" alt="DALI" class="inline-block w-8 h-8 mr-2">
        Live Traffic Agents
        🚦
    </h2>

    <div class="mb-4 flex gap-2 items-center">
        <span>
            <strong>Scenario:</strong>
        </span>

        <button wire:click="setScenario('happy')" class="px-4 bg-white text-black font-bold rounded cursor-pointer">
            <div class="flex items-center"><span class="w-3 h-3 mr-1 bg-green-500 rounded-full"></span>Main Scenario
            </div>
        </button>

        <button wire:click="setScenario('edge-accident')"
            class="px-4 bg-white text-black font-bold rounded cursor-pointer">
            <div class="flex items-center"><span class="w-3 h-3 mr-1 bg-red-500 rounded-full"></span>Accident
                Scenario</div>
        </button>

        {{-- <button wire:click="setScenario('edge-traffic')"
            class="px-4 bg-white text-black font-bold rounded cursor-pointer">
            <div class="flex items-center"><span class="w-3 h-3 mr-1 bg-yellow-400 rounded-full"></span>Traffic
                Scenario</div>
        </button>
        <button wire:click="setScenario('edge-weather')"
            class="px-4 bg-white text-black font-bold rounded cursor-pointer">
            <div class="flex items-center"><span class="w-3 h-3 mr-1 bg-yellow-400 rounded-full"></span>Weather
                Scenario</div>
        </button>
        <button wire:click="setScenario('edge-parking')"
            class="px-4 bg-white text-black font-bold rounded cursor-pointer">
            <div class="flex items-center"><span class="w-3 h-3 mr-1 bg-yellow-400 rounded-full"></span>Parking
                Scenario</div>
        </button>
        <button wire:click="setScenario('edge-delay')"
            class="px-4 bg-white text-black font-bold rounded cursor-pointer">
            <div class="flex items-center"><span class="w-3 h-3 mr-1 bg-yellow-400 rounded-full"></span>Delay
                Scenario</div>
        </button> --}}
    </div>

    <div class="text-sm text-white mb-2">
        {{-- Current scenario: <strong>{{ $currentScenario ?: 'None' }}</strong> --}}
    </div>

    <div class="flex gap-4 items-center text-sm mb-2 justify-end">
        <div class="flex items-center"><span class="w-3 h-3 mr-1 bg-green-500 rounded-full"></span>Normal</div>
        <div class="flex items-center"><span class="w-3 h-3 mr-1 bg-yellow-400 rounded-full"></span>Warning
        </div>
        <div class="flex items-center"><span class="w-3 h-3 mr-1 bg-red-500 rounded-full"></span>Incident</div>
    </div>

    <div class="flex gap-4">
        <div style="overflow: scroll; max-height: 800px; scrollbar-width: 5px; scrollbar-gutter: 5px;"
            class="w-1/8 border border-gray-100">
            <table class="table-auto w-full">
                <tbody>
                    <script>
                        const agents = @json($agents);
                    </script>
                    @foreach ($agents as $agent)
                        @php
                            $backgroundColor = match ($agent->state) {
                                'R' => '#f56565',
                                'G' => '#68d391',
                                'B' => '#63b3ed',
                                'Y' => '#f6e05e',
                                default => '#edf2f7',
                            };

                            $textColor = match ($agent->state) {
                                'R' => 'black',
                                'G' => 'black',
                                'B' => 'black',
                                'Y' => 'black',
                                default => 'black',
                            };

                            $stateText = match ($agent->state) {
                                'R' => 'Red',
                                'G' => 'Green',
                                'B' => 'Blue',
                                'Y' => 'Yellow',
                                default => 'Unknown',
                            };
                        @endphp
                        <tr class="" id="agent-row-{{ $agent->code }}">
                            <td
                                class="border border-gray-900 px-2 flex items-center gap-2  text-sm cursor-pointer hover:bg-white hover:text-black">
                                <span class="agent-indicator  inline-block w-4 h-4" data-code="{{ $agent->code }}"
                                    style="background-color: {{ $backgroundColor }}; color: {{ $textColor }}; transition: background-color 0.3s ease; border-radius: 50%; padding: 2px;">
                                </span>
                                <span class="agent-name truncate">{{ $agent->name }}</span>
                            </td>
                        </tr>
                    @endforeach
                </tbody>
            </table>
        </div>
        <div class="w-full">
            @php
                $firstAgent = $agents->first();
            @endphp

            {{-- <gmp-map center="{{$firstAgent->latitude}},{{$firstAgent->longitude}}" zoom="12" map-id="DEMO_MAP_ID"
                style="height: 800px">
                @foreach ($agents as $agent)
                @php
                $pos = "{$agent->latitude},{$agent->longitude}";
                @endphp
                <gmp-advanced-marker position="{{ $pos }}" title="{{ $agent->name }}" data-name="{{ $agent->name }}"
                    data-state="{{ $agent->state }}" data-color="{{ $agent->state === 'R' ? '#FBBC04' : '#34A853' }}"
                    data-glyph="{{ strtoupper(substr($agent->name, 0, 1)) }}"></gmp-advanced-marker>
                @endforeach
            </gmp-map> --}}

            <div id="map" style="height: 800px;"></div>

            @php
                $apiKey = env('GOOGLE_MAPS_API_KEY');
            @endphp


            <script src="https://maps.googleapis.com/maps/api/js?key={{ $apiKey }}&libraries=maps,marker&v=beta"
                defer></script>

            <script defer>
                const firstAgent = @json($firstAgent);
                window.addEventListener('DOMContentLoaded', () => {
                    let map;
                    const markers = new Map();

                    const stateColors = {
                        // G: '#34A853', // Green
                        // Y: '#F9AB00', // Yellow
                        // R: '#EA4335',  // Red
                        // B: '#63b3ed', // Blue

                        G: '#68d391', // Green
                        R: '#f56565', // Red
                        B: '#63b3ed', // Blue
                        Y: '#f6e05e', // Yellow
                        default: '#edf2f7'
                    };

                    // Wait for gmp-advanced-marker elements to be available
                    window.initMap = () => {
                        map = new google.maps.Map(document.getElementById("map"), {
                            center: { lat: parseFloat(firstAgent.latitude), lng: parseFloat(firstAgent.longitude) },
                            zoom: 12,
                            mapId: 'CargoConnect',
                        });

                        fetchAndUpdateMarkers(); // initial load
                        setInterval(fetchAndUpdateMarkers, 1000); // update every 1s
                    }

                    function focusAgent(code) {
                        const marker = markers.get(code);
                        if (marker) {
                            map.panTo(marker.position);
                            map.setZoom(15);
                        }
                    }
                    let openInfoWindow = null;

                    function addHoverListener(marker, agent) {
                        const infowindow = new google.maps.InfoWindow({
                            content: `
                                <div style="background-color: #000; color: #fff; padding: 8px; border-radius: 4px;">
                                    <strong>${agent.name}</strong><br>
                                    Type: ${agent.type}<br>
                                    Status: ${agent.state}
                                </div>
                            `,
                        });
                        marker.addListener('click', () => {
                            // infowindow.open(map, marker);

                            if (openInfoWindow) openInfoWindow.close();
                            infowindow.open(map, marker);
                            openInfoWindow = infowindow;
                        });
                    }

                    const fetchAndUpdateMarkers = async () => {
                        const res = await fetch('/live-agents');
                        const { agents } = await res.json();
                        // console.log('agents', agents);

                        agents.forEach(agent => {
                            const state = agent.state;
                            const background = stateColors[state] || '#999';
                            // const glyph = marker.dataset.glyph || state || '';
                            const glyph = state || '';
                            const key = agent.code;

                            if (markers.has(key)) {
                                // Update existing marker
                                const marker = markers.get(key);
                                const pin = new google.maps.marker.PinElement({
                                    background,
                                    glyph,
                                    glyphColor: 'black',
                                    scale: 1.2,
                                });
                                marker.content = pin.element;

                                addHoverListener(marker, agent);
                            } else {
                                const pin = new google.maps.marker.PinElement({
                                    background,
                                    glyph,
                                    glyphColor: 'black',
                                    scale: 1.2,
                                });

                                const newMarker = new google.maps.marker.AdvancedMarkerElement({
                                    map,
                                    position: {
                                        lat: agent.latitude,
                                        lng: agent.longitude
                                    },
                                    title: agent.name,
                                    content: pin.element,
                                });

                                // marker.appendChild(pin.element);
                                markers.set(key, newMarker);

                                addHoverListener(newMarker, agent);
                            }

                            // 🔁 Also update the table row color
                            const indicator = document.querySelector(`.agent-indicator[data-code="${agent.code}"]`);
                            if (indicator) {
                                indicator.style.backgroundColor = background;
                            }

                        });
                    };

                    // const timeInterval = 50;
                    const timeInterval = 100;

                    // Poll for gmp-map readiness (Google Maps Beta)
                    const checkMapReady = setInterval(() => {
                        // if (window.google && window.google.maps && window.google.maps.marker) {
                        //     clearInterval(checkMapReady);
                        //     initMap();
                        // }

                        if (window.google?.maps?.marker?.PinElement) {
                            clearInterval(checkMapReady);
                            initMap();
                        }
                    }, timeInterval);
                });
            </script>
        </div>
    </div>
</div>
</script>