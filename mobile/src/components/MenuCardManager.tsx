import React, { useCallback, useEffect, useState } from "react";
import { ActivityIndicator, Alert } from "react-native";
import { StoreApi } from "../api/endpoints";
import { apiUrl, ApiError } from "../api/client";
import { MenuImage } from "../api/types";
import { color } from "../theme/tokens";
import { Text } from "./Text";
import { useToast } from "./Toast";
import { MenuPhotoPicker } from "./MenuPhotoPicker";

/**
 * A restaurant's live menu card on the Shop tab: every change is saved as it's
 * made, so there's no "save" to forget.
 */
export function MenuCardManager({ storeId }: { storeId: string }) {
  const toast = useToast();
  const [pages, setPages] = useState<MenuImage[] | null>(null);
  const [busy, setBusy] = useState(false);

  const load = useCallback(async () => {
    try {
      setPages(await StoreApi.menuImages(storeId));
    } catch {
      setPages([]);
    }
  }, [storeId]);

  useEffect(() => {
    load();
  }, [load]);

  async function add(uris: string[]) {
    setBusy(true);
    let failed = 0;
    let lastError: string | null = null;
    for (const uri of uris) {
      try {
        await StoreApi.uploadMenuImage(uri);
      } catch (e) {
        failed++;
        if (e instanceof ApiError) lastError = e.fieldErrors[0]?.message ?? e.message;
      }
    }
    await load();
    setBusy(false);
    if (failed) {
      toast(lastError ?? `${failed} page${failed === 1 ? "" : "s"} didn't upload. Try again.`, "urgent");
    } else {
      toast(uris.length === 1 ? "Menu page added" : `${uris.length} menu pages added`, "positive");
    }
  }

  function remove(imageId: string) {
    const page = pages?.find((p) => p.imageId === imageId)?.page;
    Alert.alert("Remove this page?", `Customers will no longer see page ${page} of your menu.`, [
      { text: "Keep", style: "cancel" },
      {
        text: "Remove",
        style: "destructive",
        onPress: async () => {
          setBusy(true);
          try {
            await StoreApi.deleteMenuImage(imageId);
            await load();
          } catch {
            toast("Couldn't remove that page. Try again.", "urgent");
          } finally {
            setBusy(false);
          }
        },
      },
    ]);
  }

  if (pages === null) return <ActivityIndicator color={color.brand[500]} />;

  return (
    <>
      <Text variant="bodySm" color={color.neutral.inkMuted}>
        {pages.length
          ? "Customers see these pages when they tap your restaurant."
          : "Add a photo of each page of your menu. Customers see it when they tap your restaurant."}
      </Text>
      <MenuPhotoPicker
        photos={pages.map((p) => ({ key: p.imageId, uri: apiUrl(p.url) }))}
        onAdd={add}
        onRemove={remove}
        busy={busy}
      />
    </>
  );
}
