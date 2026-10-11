import React, { useCallback, useEffect, useState } from "react";
import {
  ActivityIndicator,
  Image,
  Modal,
  Pressable,
  ScrollView,
  StyleSheet,
  useWindowDimensions,
  View,
} from "react-native";
import { useFocusEffect, useNavigation, useRoute } from "@react-navigation/native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { AppBar } from "../../components/AppBar";
import { Card } from "../../components/Card";
import { Text } from "../../components/Text";
import { Button } from "../../components/Button";
import { PressableScale } from "../../components/Motion";
import { StoreApi } from "../../api/endpoints";
import { apiUrl } from "../../api/client";
import { MenuImage } from "../../api/types";
import { color, font, radius, spacing } from "../../theme/tokens";
import { contentWidth } from "../../theme/layoutStyles";

type Params = { storeId: string; storeName: string };

/**
 * A restaurant's page: its menu card, page by page, exactly as the owner
 * photographed it, and a way to order from it.
 *
 * Every state says something — loading, no menu yet, a page that won't load —
 * because a blank or black screen reads as "the app is broken", not "this
 * restaurant hasn't added its menu".
 */
export function StoreMenuScreen() {
  const nav = useNavigation<any>();
  const insets = useSafeAreaInsets();
  const { storeId, storeName } = useRoute().params as Params;

  const [pages, setPages] = useState<MenuImage[] | null>(null);
  const [failed, setFailed] = useState(false);
  const [open, setOpen] = useState<number | null>(null);

  const load = useCallback(async () => {
    setFailed(false);
    try {
      setPages(await StoreApi.menuImages(storeId));
    } catch {
      setFailed(true);
      setPages([]);
    }
  }, [storeId]);

  useFocusEffect(
    useCallback(() => {
      load();
    }, [load])
  );

  const order = () =>
    nav.navigate("CreateBasket", { category: "FOOD", storeIds: [storeId] });

  return (
    <View style={styles.flex}>
      <AppBar title={storeName} subtitle="🍽️ Restaurant" onBack={() => nav.goBack()} />

      <ScrollView
        contentContainerStyle={[styles.content, contentWidth.column]}
        showsVerticalScrollIndicator={false}
      >
        <Text variant="h2">Menu</Text>

        {pages === null ? (
          <Card elevated style={styles.stateCard}>
            <ActivityIndicator color={color.brand[500]} />
            <Text variant="bodySm" color={color.neutral.inkMuted}>
              Loading the menu…
            </Text>
          </Card>
        ) : failed ? (
          <Card elevated style={styles.stateCard}>
            <Text style={styles.stateEmoji}>📶</Text>
            <Text variant="h3">Couldn't load the menu</Text>
            <Text variant="bodySm" color={color.neutral.inkMuted} style={styles.center}>
              Check your connection and try again.
            </Text>
            <Button label="Try again" variant="secondary" onPress={load} fullWidth={false} />
          </Card>
        ) : pages.length === 0 ? (
          <Card elevated style={styles.stateCard}>
            <Text style={styles.stateEmoji}>📋</Text>
            <Text variant="h3">No menu card yet</Text>
            <Text variant="bodySm" color={color.neutral.inkMuted} style={styles.center}>
              {storeName} hasn't added photos of its menu. You can still order — tell
              them what you'd like and they'll say what they can make.
            </Text>
          </Card>
        ) : (
          <>
            <Text variant="bodySm" color={color.neutral.inkFaint}>
              {pages.length} page{pages.length === 1 ? "" : "s"} · tap a page to zoom in
            </Text>
            {pages.map((p, i) => (
              <PressableScale
                key={p.imageId}
                onPress={() => setOpen(i)}
                accessibilityLabel={`Menu page ${p.page}, open full screen`}
              >
                <MenuPage uri={apiUrl(p.url)} page={p.page} />
              </PressableScale>
            ))}
          </>
        )}
      </ScrollView>

      <View style={[styles.footer, { paddingBottom: insets.bottom + spacing.sm }]}>
        <Button label="Order from this restaurant" onPress={order} />
      </View>

      {pages && open !== null ? (
        <FullScreenMenu
          pages={pages}
          index={open}
          onIndex={setOpen}
          onClose={() => setOpen(null)}
        />
      ) : null}
    </View>
  );
}

/** A page with its own loading and error states, sized to the photo once known. */
function MenuPage({ uri, page }: { uri: string; page: number }) {
  const [ratio, setRatio] = useState(0.72);
  const [state, setState] = useState<"loading" | "ready" | "error">("loading");
  const [attempt, setAttempt] = useState(0);

  // Image.getSize works on every platform; onLoad's size is missing on web.
  useEffect(() => {
    Image.getSize(
      uri,
      (w, h) => {
        if (w && h) setRatio(w / h);
      },
      () => {}
    );
  }, [uri]);

  return (
    <View style={styles.page}>
      <Image
        key={attempt}
        source={{ uri }}
        style={[styles.pageImage, { aspectRatio: ratio }]}
        resizeMode="contain"
        onLoad={() => setState("ready")}
        onError={() => setState("error")}
      />
      {state !== "ready" ? (
        <View style={styles.pageOverlay}>
          {state === "loading" ? (
            <ActivityIndicator color={color.brand[500]} />
          ) : (
            <Pressable
              onPress={() => {
                setState("loading");
                setAttempt((n) => n + 1);
              }}
              accessibilityRole="button"
              style={styles.center}
            >
              <Text variant="bodySm" color={color.neutral.inkMuted} style={styles.center}>
                Couldn't load page {page}.
              </Text>
              <Text variant="bodySm" weight="semibold" color={color.brand[600]}>
                Tap to retry
              </Text>
            </Pressable>
          )}
        </View>
      ) : null}
      <Text style={styles.pageNo}>{page}</Text>
    </View>
  );
}

/**
 * One page at a time, fitted to the screen, with previous/next buttons. iOS
 * also pinch-zooms. A page that won't load says so instead of staying black.
 */
function FullScreenMenu({
  pages,
  index,
  onIndex,
  onClose,
}: {
  pages: MenuImage[];
  index: number;
  onIndex: (i: number) => void;
  onClose: () => void;
}) {
  const { width, height } = useWindowDimensions();
  const insets = useSafeAreaInsets();
  const [state, setState] = useState<"loading" | "ready" | "error">("loading");
  const page = pages[index];

  const go = (next: number) => {
    setState("loading");
    onIndex(next);
  };

  return (
    <Modal visible animationType="fade" onRequestClose={onClose} supportedOrientations={["portrait", "landscape"]}>
      <View style={styles.viewer}>
        <ScrollView
          key={page.imageId}
          style={styles.viewerScroll}
          contentContainerStyle={styles.viewerPage}
          maximumZoomScale={4}
          minimumZoomScale={1}
          centerContent
          showsHorizontalScrollIndicator={false}
          showsVerticalScrollIndicator={false}
        >
          <Image
            source={{ uri: apiUrl(page.url) }}
            style={{ width, height: height - insets.top - insets.bottom - 140 }}
            resizeMode="contain"
            onLoad={() => setState("ready")}
            onError={() => setState("error")}
          />
        </ScrollView>

        {state !== "ready" ? (
          <View style={styles.viewerOverlay} pointerEvents="none">
            {state === "loading" ? (
              <ActivityIndicator color="#fff" size="large" />
            ) : (
              <Text style={styles.viewerText}>Couldn't load this page.</Text>
            )}
          </View>
        ) : null}

        <View style={[styles.viewerBar, { top: insets.top + spacing.sm }]}>
          <Text style={styles.viewerText}>
            Page {index + 1} of {pages.length}
          </Text>
          <Pressable
            onPress={onClose}
            hitSlop={12}
            accessibilityRole="button"
            accessibilityLabel="Close menu"
            style={styles.viewerBtn}
          >
            <Text style={styles.viewerBtnText}>×</Text>
          </Pressable>
        </View>

        {pages.length > 1 ? (
          <View style={[styles.viewerNav, { bottom: insets.bottom + spacing.lg }]}>
            <Pressable
              onPress={() => go(index - 1)}
              disabled={index === 0}
              accessibilityRole="button"
              accessibilityLabel="Previous page"
              style={[styles.viewerPill, index === 0 && styles.viewerPillOff]}
            >
              <Text style={styles.viewerText}>‹  Previous</Text>
            </Pressable>
            <Pressable
              onPress={() => go(index + 1)}
              disabled={index === pages.length - 1}
              accessibilityRole="button"
              accessibilityLabel="Next page"
              style={[styles.viewerPill, index === pages.length - 1 && styles.viewerPillOff]}
            >
              <Text style={styles.viewerText}>Next  ›</Text>
            </Pressable>
          </View>
        ) : null}
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1, backgroundColor: color.neutral.background },
  content: {
    padding: spacing.lg,
    paddingTop: 0,
    gap: spacing.sm,
    paddingBottom: spacing.xl,
  },
  center: { textAlign: "center", alignItems: "center" },
  stateCard: { alignItems: "center", gap: spacing.xs, paddingVertical: spacing.xl },
  stateEmoji: { fontSize: 32, lineHeight: 40 },
  page: {
    borderRadius: radius.lg,
    overflow: "hidden",
    backgroundColor: color.neutral.surface,
    borderWidth: 1,
    borderColor: color.neutral.border,
  },
  pageImage: { width: "100%" },
  pageOverlay: {
    ...StyleSheet.absoluteFill,
    alignItems: "center",
    justifyContent: "center",
    padding: spacing.lg,
    backgroundColor: color.neutral.surface,
  },
  pageNo: {
    position: "absolute",
    left: spacing.sm,
    top: spacing.sm,
    fontFamily: font.body.bold,
    fontSize: 12,
    lineHeight: 16,
    color: "#fff",
    backgroundColor: "rgba(20,25,20,0.6)",
    borderRadius: radius.pill,
    paddingHorizontal: 8,
    paddingVertical: 2,
    overflow: "hidden",
  },
  footer: {
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.sm,
    backgroundColor: color.neutral.background,
    borderTopWidth: 1,
    borderTopColor: color.neutral.border,
  },
  viewer: { flex: 1, backgroundColor: "#111" },
  viewerScroll: { flex: 1 },
  viewerPage: { flexGrow: 1, alignItems: "center", justifyContent: "center" },
  viewerOverlay: {
    ...StyleSheet.absoluteFill,
    alignItems: "center",
    justifyContent: "center",
  },
  viewerBar: {
    position: "absolute",
    left: spacing.lg,
    right: spacing.lg,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
  },
  viewerText: { fontFamily: font.body.semibold, fontSize: 14, lineHeight: 20, color: "#fff" },
  viewerBtn: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: "rgba(255,255,255,0.18)",
    alignItems: "center",
    justifyContent: "center",
  },
  viewerBtnText: { color: "#fff", fontSize: 26, lineHeight: 28, fontFamily: font.body.bold },
  viewerNav: {
    position: "absolute",
    left: spacing.lg,
    right: spacing.lg,
    flexDirection: "row",
    justifyContent: "space-between",
  },
  viewerPill: {
    paddingHorizontal: spacing.md,
    height: 44,
    borderRadius: radius.pill,
    backgroundColor: "rgba(255,255,255,0.18)",
    justifyContent: "center",
  },
  viewerPillOff: { opacity: 0.35 },
});
