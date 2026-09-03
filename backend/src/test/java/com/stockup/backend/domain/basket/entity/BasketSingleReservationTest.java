package com.stockup.backend.domain.basket.entity;

import com.stockup.backend.domain.basket.enums.BasketStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * A shopping list is collected from exactly one shop.
 *
 * {@code reserve()} existed but nothing ever called it, so the list stayed
 * ACTIVE after a successful reservation and the customer could accept every
 * reply they got. Three shopkeepers would each hold stock for one basket, and
 * the two who lost out wore the Bharosa penalty for an order that was never
 * theirs to complete.
 */
class BasketSingleReservationTest {

    /** A list open to offers: created, then broadcast to nearby shops. */
    private Basket list() {
        Basket basket = Basket.create(null, null, null, null, null);
        basket.publish();
        return basket;
    }

    @Test
    void startsOpenToOffers() {
        assertThat(list().getStatus()).isEqualTo(BasketStatus.ACTIVE);
    }

    @Test
    void reservingClosesTheList() {
        Basket basket = list();

        basket.reserve();

        assertThat(basket.getStatus()).isEqualTo(BasketStatus.RESERVED);
    }

    @Test
    void aListCannotBeReservedAtASecondShop() {
        Basket basket = list();
        basket.reserve();

        assertThatThrownBy(basket::reserve)
                .as("the second shop must be refused, not silently accepted")
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void aReservedListCannotThenBeCancelledAsIfStillOpen() {
        Basket basket = list();
        basket.reserve();

        assertThatThrownBy(basket::cancel).isInstanceOf(RuntimeException.class);
    }

    @Test
    void aCancelledReservationHandsTheListBack() {
        Basket basket = list();
        basket.reserve();

        basket.reopen();

        assertThat(basket.getStatus())
                .as("cancelling inside the grace window must not strand the list")
                .isEqualTo(BasketStatus.ACTIVE);
    }

    @Test
    void aReopenedListCanBeReservedSomewhereElse() {
        Basket basket = list();
        basket.reserve();
        basket.reopen();

        basket.reserve();

        assertThat(basket.getStatus()).isEqualTo(BasketStatus.RESERVED);
    }

    @Test
    void anOpenListCannotBeReopened() {
        assertThatThrownBy(list()::reopen)
                .as("reopen is an undo, not a way to revive an expired list")
                .isInstanceOf(RuntimeException.class);
    }
}
