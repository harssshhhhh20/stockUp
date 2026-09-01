import React from "react";
import Svg, { Path, Rect, Circle } from "react-native-svg";

/**
 * Hand-drawn-feeling line icons with rounded caps — deliberately softer than
 * the sharp geometric icon sets a generic dashboard would use, matching the
 * rounded display type.
 */
export function TabIcon({
  route,
  focused,
  color: c,
}: {
  route: string;
  focused: boolean;
  color: string;
}) {
  const sw = focused ? 2.3 : 1.9;
  const common = {
    stroke: c,
    strokeWidth: sw,
    strokeLinecap: "round" as const,
    strokeLinejoin: "round" as const,
    fill: "none",
  };

  return (
    <Svg width={24} height={24} viewBox="0 0 24 24">
      {route === "Lists" || route === "Requests" ? (
        <>
          <Path d="M4.5 8h15l-1.4 10.2a2 2 0 0 1-2 1.8H7.9a2 2 0 0 1-2-1.8L4.5 8Z" {...common} />
          <Path d="M9 8V6.2A3 3 0 0 1 12 3.2a3 3 0 0 1 3 3V8" {...common} />
        </>
      ) : null}

      {route === "Orders" ? (
        <>
          <Rect x="4.5" y="3.5" width="15" height="17" rx="2.5" {...common} />
          <Path d="M8.5 9h7M8.5 13h7M8.5 17h4" {...common} />
        </>
      ) : null}

      {route === "Updates" ? (
        <>
          <Path
            d="M6.5 10.5a5.5 5.5 0 0 1 11 0c0 3.2.8 5 1.5 6H5c.7-1 1.5-2.8 1.5-6Z"
            {...common}
          />
          <Path d="M10 20a2.2 2.2 0 0 0 4 0" {...common} />
        </>
      ) : null}

      {route === "You" ? (
        <>
          <Circle cx="12" cy="8.5" r="3.6" {...common} />
          <Path d="M4.8 20c.6-3.7 3.6-5.7 7.2-5.7s6.6 2 7.2 5.7" {...common} />
        </>
      ) : null}
    </Svg>
  );
}
