package com.stockup.backend.domain.store.service.impl;

import com.stockup.backend.common.exceptions.model.BadRequestException;
import com.stockup.backend.common.exceptions.model.ResourceNotFoundException;
import com.stockup.backend.common.security.CurrentUserService;
import com.stockup.backend.domain.merchant.repository.MerchantRepository;
import com.stockup.backend.domain.store.dto.response.MenuImageContent;
import com.stockup.backend.domain.store.dto.response.MenuImageResponse;
import com.stockup.backend.domain.store.entity.Store;
import com.stockup.backend.domain.store.entity.StoreMenuImage;
import com.stockup.backend.domain.store.entity.enums.BusinessType;
import com.stockup.backend.domain.store.repository.StoreMenuImageRepository;
import com.stockup.backend.domain.store.repository.StoreRepository;
import com.stockup.backend.domain.store.service.StoreMenuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@Transactional
public class StoreMenuServiceImpl implements StoreMenuService {

    /** A real menu card is a few pages; more than this is a gallery, not a menu. */
    static final int MAX_PAGES = 10;

    /** The phone resizes to ~1600px before upload, which lands well under this. */
    static final int MAX_BYTES = 5 * 1024 * 1024;

    private final StoreMenuImageRepository menuImageRepository;
    private final StoreRepository storeRepository;
    private final MerchantRepository merchantRepository;
    private final CurrentUserService currentUserService;

    public StoreMenuServiceImpl(
            StoreMenuImageRepository menuImageRepository,
            StoreRepository storeRepository,
            MerchantRepository merchantRepository,
            CurrentUserService currentUserService
    ) {
        this.menuImageRepository = menuImageRepository;
        this.storeRepository = storeRepository;
        this.merchantRepository = merchantRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    public MenuImageResponse uploadToMyStore(MultipartFile file) {

        Store store = myStore();

        if (store.getBusinessType() != BusinessType.RESTAURANT) {
            throw new BadRequestException("Only restaurants can add a menu card.");
        }
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("That photo was empty.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BadRequestException("That photo is too large. Keep each page under 5 MB.");
        }
        if (menuImageRepository.countByStore_Id(store.getId()) >= MAX_PAGES) {
            throw new BadRequestException("A menu card can have at most " + MAX_PAGES + " pages.");
        }

        byte[] data;
        try {
            data = file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("Couldn't read that photo. Try again.");
        }

        // Judged from the bytes, not the client's Content-Type: this endpoint's
        // output is served back to every customer as an image.
        String contentType = detectImageType(data);
        if (contentType == null) {
            throw new BadRequestException("Menu pages must be JPEG, PNG or WebP photos.");
        }

        int page = menuImageRepository.maxSortOrder(store.getId()) + 1;
        StoreMenuImage saved = menuImageRepository.save(
                new StoreMenuImage(store, contentType, data, page)
        );

        return toResponse(store.getId(), saved.getId(), page);
    }

    @Override
    public void deleteFromMyStore(UUID imageId) {

        Store store = myStore();

        StoreMenuImage image = menuImageRepository.findByIdAndStore_Id(imageId, store.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu page not found."));

        menuImageRepository.delete(image);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuImageResponse> list(UUID storeId) {

        List<StoreMenuImageRepository.Summary> summaries = menuImageRepository.findSummaries(storeId);

        // Number pages by position, so deleting page 2 doesn't leave a gap.
        return IntStream.range(0, summaries.size())
                .mapToObj(i -> toResponse(storeId, summaries.get(i).getId(), i + 1))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MenuImageContent content(UUID storeId, UUID imageId) {

        StoreMenuImage image = menuImageRepository.findByIdAndStore_Id(imageId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu page not found."));

        return new MenuImageContent(image.getContentType(), image.getData());
    }

    private Store myStore() {
        return merchantRepository.findByUser(currentUserService.getCurrentUser())
                .flatMap(storeRepository::findByMerchant)
                .orElseThrow(() -> new ResourceNotFoundException("Set up your shop first."));
    }

    private static MenuImageResponse toResponse(UUID storeId, UUID imageId, int page) {
        return new MenuImageResponse(
                imageId,
                page,
                "/api/v1/stores/" + storeId + "/menu-images/" + imageId
        );
    }

    /** Magic-number sniffing for the three formats a phone camera produces. */
    static String detectImageType(byte[] d) {
        if (d.length >= 3
                && (d[0] & 0xFF) == 0xFF && (d[1] & 0xFF) == 0xD8 && (d[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (d.length >= 8
                && (d[0] & 0xFF) == 0x89 && d[1] == 'P' && d[2] == 'N' && d[3] == 'G') {
            return "image/png";
        }
        if (d.length >= 12
                && d[0] == 'R' && d[1] == 'I' && d[2] == 'F' && d[3] == 'F'
                && d[8] == 'W' && d[9] == 'E' && d[10] == 'B' && d[11] == 'P') {
            return "image/webp";
        }
        return null;
    }
}
