package com.stavia.service;

import com.stavia.dto.order.OrderCreateDto;
import com.stavia.dto.order.OrderItemDto;
import com.stavia.dto.order.OrderResponseDto;
import com.stavia.entity.*;
import com.stavia.exception.ResourceNotFoundException;
import com.stavia.mapper.OrderMapper;
import com.stavia.repository.OrderRepository;
import com.stavia.repository.TableReservationRepository;
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
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TableReservationRepository tableReservationRepository;

    @Mock
    private MenuItemService menuItemService;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService service;

    @Test
    void createSuccess() {

        OrderItemDto itemDto = new OrderItemDto();
        itemDto.setMenuItemId(1L);
        itemDto.setQuantity(2);

        OrderCreateDto dto = new OrderCreateDto();
        dto.setTableReservationId(1L);
        dto.setItems(List.of(itemDto));

        TableReservation reservation = new TableReservation();

        MenuItem menuItem = new MenuItem();
        menuItem.setPrice(10.0);

        when(tableReservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(menuItemService.findById(1L))
                .thenReturn(menuItem);

        when(orderRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        when(orderMapper.toResponseDto(any()))
                .thenReturn(new OrderResponseDto());

        OrderResponseDto response = service.create(dto);

        assertNotNull(response);

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void getUserOrdersSuccess() {
        Pageable pageable = PageRequest.of(0, 10);

        when(orderRepository.findAllByTableReservationUserIdAndDeletedFalse(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(new Order())));
        when(orderMapper.toResponseDto(any())).thenReturn(new OrderResponseDto());

        Page<OrderResponseDto> result = service.getUserOrders(1L, pageable);

        assertEquals(1, result.getTotalElements());
        verify(orderRepository).findAllByTableReservationUserIdAndDeletedFalse(1L, pageable);
    }

    @Test
    void createShouldThrowWhenReservationNotFound() {

        OrderCreateDto dto = new OrderCreateDto();
        dto.setTableReservationId(1L);

        when(tableReservationRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.create(dto));
    }

    @Test
    void getAllOrdersSuccess() {

        Pageable pageable = PageRequest.of(0, 10);

        when(orderRepository.findAllByDeletedFalse(pageable))
                .thenReturn(new PageImpl<>(List.of(new Order())));

        when(orderMapper.toResponseDto(any()))
                .thenReturn(new OrderResponseDto());

        Page<OrderResponseDto> result = service.getAllOrders(pageable);

        assertEquals(1, result.getTotalElements());
    }
}