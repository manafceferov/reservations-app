package com.stavia.service;

import com.stavia.dto.menuitem.MenuItemCreateDto;
import com.stavia.dto.menuitem.MenuItemResponseDto;
import com.stavia.entity.MenuItem;
import com.stavia.exception.ResourceNotFoundException;
import com.stavia.mapper.MenuItemMapper;
import com.stavia.repository.MenuItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuItemServiceTest {

    @Mock
    private MenuItemRepository repository;

    @Mock
    private MenuItemMapper mapper;

    @InjectMocks
    private MenuItemService service;

    @Test
    void createSuccess() {
        MenuItemCreateDto dto = new MenuItemCreateDto();
        dto.setName("Pizza");

        MenuItem item = new MenuItem();
        item.setName("Pizza");

        MenuItemResponseDto responseDto = new MenuItemResponseDto();

        when(repository.save(any())).thenReturn(item);
        when(mapper.toResponseDto(any())).thenReturn(responseDto);

        MenuItemResponseDto result = service.create(dto);

        assertNotNull(result);

        verify(repository).save(any(MenuItem.class));
        verify(mapper).toResponseDto(any(MenuItem.class));
    }

    @Test
    void getAllSuccess() {
        Pageable pageable = PageRequest.of(0, 10);

        MenuItem item = new MenuItem();

        when(repository.findAllByDeletedFalse(pageable))
                .thenReturn(new PageImpl<>(List.of(item)));

        when(mapper.toResponseDto(any()))
                .thenReturn(new MenuItemResponseDto());

        Page<MenuItemResponseDto> result = service.getAll(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void deleteSuccess() {
        MenuItem item = new MenuItem();

        when(repository.findById(1L))
                .thenReturn(Optional.of(item));

        service.delete(1L);

        assertTrue(item.getDeleted());

        verify(repository).save(item);
    }

    @Test
    void findByIdShouldThrow() {
        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.findById(1L));
    }

    @Test
    void createBatchSuccess() {
        MenuItemCreateDto dto = new MenuItemCreateDto();
        dto.setName("Burger");

        when(repository.saveAll(anyList())).thenReturn(List.of(new MenuItem()));
        when(mapper.toResponseDto(any())).thenReturn(new MenuItemResponseDto());

        List<MenuItemResponseDto> result = service.createBatch(List.of(dto));

        assertFalse(result.isEmpty());
        verify(repository).saveAll(anyList());
    }

    @Test
    void getByCategorySuccess() {
        when(repository.findAllByCategoryAndAvailableTrueAndDeletedFalse("FastFood"))
                .thenReturn(List.of(new MenuItem()));
        when(mapper.toResponseDto(any())).thenReturn(new MenuItemResponseDto());

        List<MenuItemResponseDto> result = service.getByCategory("FastFood");

        assertEquals(1, result.size());
    }

    @Test
    void editSuccess() {
        MenuItem item = new MenuItem();
        item.setName("Old Name");

        MenuItemCreateDto dto = new MenuItemCreateDto();
        dto.setName("New Name");
        dto.setDescription("New Desc");

        when(repository.findById(1L)).thenReturn(Optional.of(item));
        when(repository.save(any())).thenReturn(item);
        when(mapper.toResponseDto(any())).thenReturn(new MenuItemResponseDto());

        service.edit(1L, dto);

        assertEquals("New Name", item.getName());
        assertEquals("New Desc", item.getDescription());
        verify(repository).save(item);
    }
}