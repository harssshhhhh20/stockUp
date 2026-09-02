import React from "react";
import { ActivityIndicator, StyleSheet, View } from "react-native";
import { NavigationContainer, DefaultTheme } from "@react-navigation/native";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import { createBottomTabNavigator } from "@react-navigation/bottom-tabs";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useAuth } from "../state/AuthContext";
import { usePushNotifications } from "../state/usePushNotifications";
import { SignInScreen } from "../screens/auth/SignInScreen";
import { BasketsScreen } from "../screens/customer/BasketsScreen";
import { CreateBasketScreen } from "../screens/customer/CreateBasketScreen";
import { BasketDetailScreen } from "../screens/customer/BasketDetailScreen";
import { ReservationDetailScreen } from "../screens/customer/ReservationDetailScreen";
import { RequestsScreen } from "../screens/merchant/RequestsScreen";
import { RespondToRequestScreen } from "../screens/merchant/RespondToRequestScreen";
import { MerchantReservationDetailScreen } from "../screens/merchant/MerchantReservationDetailScreen";
import { ReservationsScreen } from "../screens/shared/ReservationsScreen";
import { NotificationsScreen } from "../screens/shared/NotificationsScreen";
import { ProfileScreen } from "../screens/shared/ProfileScreen";
import { OrderDetailScreen } from "../screens/shared/OrderDetailScreen";
import { RateOrderScreen } from "../screens/customer/RateOrderScreen";
import { PickShopsScreen } from "../screens/customer/PickShopsScreen";
import { BecomeMerchantScreen } from "../screens/onboarding/BecomeMerchantScreen";
import { TabIcon } from "../components/TabIcon";
import { color, font } from "../theme/tokens";

const Stack = createNativeStackNavigator();
const Tab = createBottomTabNavigator();

const navTheme = {
  ...DefaultTheme,
  colors: {
    ...DefaultTheme.colors,
    background: color.neutral.background,
    card: color.neutral.surface,
    border: color.neutral.border,
    primary: color.brand[500],
    text: color.neutral.ink,
  },
};

function Tabs() {
  const { mode, merchantProfile, store } = useAuth();
  const insets = useSafeAreaInsets();
  const isMerchant = mode === "merchant" && !!merchantProfile && !!store;

  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        headerShown: false,
        tabBarActiveTintColor: color.brand[600],
        tabBarInactiveTintColor: color.neutral.inkFaint,
        tabBarStyle: [
          styles.tabBar,
          { height: 70 + insets.bottom, paddingBottom: insets.bottom + 12 },
        ],
        tabBarLabelStyle: styles.tabLabel,
        tabBarIcon: ({ focused, color: c }) => (
          <TabIcon route={route.name} focused={focused} color={c} />
        ),
      })}
    >
      {isMerchant ? (
        <Tab.Screen name="Requests" component={RequestsScreen} />
      ) : (
        <Tab.Screen name="Lists" component={BasketsScreen} />
      )}
      <Tab.Screen
        name="Orders"
        component={ReservationsScreen}
        options={{ tabBarLabel: isMerchant ? "Pickups" : "Orders" }}
      />
      <Tab.Screen name="Updates" component={NotificationsScreen} />
      <Tab.Screen name="You" component={ProfileScreen} />
    </Tab.Navigator>
  );
}

export function RootNavigator() {
  const { booting, signedIn } = useAuth();
  // Register for push once signed in; failures are silent by design.
  usePushNotifications(signedIn);

  if (booting) {
    return (
      <View style={styles.boot}>
        <ActivityIndicator size="large" color={color.brand[500]} />
      </View>
    );
  }

  return (
    <NavigationContainer theme={navTheme}>
      <Stack.Navigator screenOptions={{ headerShown: false }}>
        {!signedIn ? (
          <Stack.Screen name="SignIn" component={SignInScreen} />
        ) : (
          <>
            <Stack.Screen name="Tabs" component={Tabs} />
            <Stack.Screen name="CreateBasket" component={CreateBasketScreen} />
            <Stack.Screen name="BasketDetail" component={BasketDetailScreen} />
            <Stack.Screen name="ReservationDetail" component={ReservationDetailScreen} />
      <Stack.Screen name="OrderDetail" component={OrderDetailScreen} />
      <Stack.Screen name="RateOrder" component={RateOrderScreen} />
      <Stack.Screen name="PickShops" component={PickShopsScreen} />
            <Stack.Screen name="RespondToRequest" component={RespondToRequestScreen} />
            <Stack.Screen
              name="MerchantReservationDetail"
              component={MerchantReservationDetailScreen}
            />
            <Stack.Screen name="BecomeMerchant" component={BecomeMerchantScreen} />
          </>
        )}
      </Stack.Navigator>
    </NavigationContainer>
  );
}

const styles = StyleSheet.create({
  boot: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: color.neutral.background,
  },
  tabBar: {
    backgroundColor: color.neutral.surface,
    borderTopColor: color.neutral.border,
    paddingTop: 8,
  },
  tabLabel: {
    fontFamily: font.body.semibold,
    fontSize: 11,
  },
});
