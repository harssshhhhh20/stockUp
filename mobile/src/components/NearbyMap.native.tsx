import React from "react";
import { StyleSheet, View } from "react-native";
import MapView, { Marker } from "react-native-maps";
import { MapProps, MapPlaceholder, mapStyles, pinTone } from "./NearbyMap.shared";
import { NearbyStore } from "../api/types";
import { Text } from "./Text";

/**
 * A compact map strip: you in the middle, shops around you.
 *
 * Deliberately short — it orients you, it isn't a navigation surface. The list
 * underneath is where the decision actually gets made, because a pin can't tell
 * you whether a shop answers its messages.
 *
 * iOS renders this with Apple Maps, which needs no API key.
 */
export function NearbyMap({ coords, stores, height = 190 }: MapProps) {
  if (!coords) return <MapPlaceholder height={height} />;

  // Frame the pins rather than fixing a zoom, so a dense market and a spread-out
  // suburb both look sensible.
  const spread = stores.reduce((max, s) => Math.max(max, s.distanceKm), 0.4);
  const delta = Math.max(0.006, (spread / 111) * 2.4);

  return (
    <View style={[mapStyles.wrap, { height }]}>
      <MapView
        style={StyleSheet.absoluteFillObject}
        initialRegion={{
          latitude: coords.latitude,
          longitude: coords.longitude,
          latitudeDelta: delta,
          longitudeDelta: delta,
        }}
        showsUserLocation
        showsMyLocationButton={false}
        toolbarEnabled={false}
        pitchEnabled={false}
        rotateEnabled={false}
      >
        {stores.map((s, i) => (
          <Marker
            key={s.storeId}
            coordinate={markerCoord(coords, s, i, stores.length)}
            title={s.name}
            description={s.knownFor}
          >
            <View style={[mapStyles.pin, pinTone(s.band)]}>
              <Text style={mapStyles.pinText}>{s.bharosa}</Text>
            </View>
          </Marker>
        ))}
      </MapView>
    </View>
  );
}

/**
 * The API returns distance, not coordinates — a shop's exact position is never
 * exposed to the client. Pins sit at the correct radius on a stable bearing, so
 * the map reads truthfully about *how far* without claiming a precision we do
 * not have.
 */
function markerCoord(
  coords: { latitude: number; longitude: number },
  store: NearbyStore,
  index: number,
  total: number
) {
  const bearing = (index / Math.max(1, total)) * 2 * Math.PI;
  const dLat = (store.distanceKm / 111) * Math.cos(bearing);
  const dLng =
    (store.distanceKm / (111 * Math.cos((coords.latitude * Math.PI) / 180))) * Math.sin(bearing);
  return { latitude: coords.latitude + dLat, longitude: coords.longitude + dLng };
}
