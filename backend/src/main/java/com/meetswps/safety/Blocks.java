package com.meetswps.safety;

import org.springframework.stereotype.Component;

import java.util.UUID;

/** Lets other features check blocks without depending on how they are stored. */
@Component
public class Blocks {

    private final BlockRepository blocks;

    Blocks(BlockRepository blocks) {
        this.blocks = blocks;
    }

    public boolean existBetween(UUID a, UUID b) {
        return blocks.existsBetween(a, b);
    }
}
