package com.example.offline.demo.service;

import com.example.offline.demo.controller.request.CreateSaleRequest;
import com.example.offline.demo.controller.response.GetAllSalesResponse;
import com.example.offline.demo.controller.response.GetMaxIdResponse;
import com.example.offline.demo.controller.response.GetSaleInfo;
import com.example.offline.demo.entity.*;
import com.example.offline.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private SaleDetailRepository saleDetailRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private SaleService saleService;

    private CreateSaleRequest validRequest;
    private ClientEntity testClient;
    private ProductEntity testProduct;
    private SaleEntity testSale;
    private SaleDetailEntity testSaleDetail;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba comunes
        testClient = new ClientEntity();
        testClient.setId(1L);
        testClient.setDocument(12345678);
        testClient.setFirstName("John");
        testClient.setLastName("Doe");

        testProduct = new ProductEntity();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(100.0);
        testProduct.setQuantity(10);

        testSale = new SaleEntity();
        testSale.setId(1L);
        testSale.setClient(testClient);
        testSale.setSaleDate(LocalDate.now());
        testSale.setTotalAmount(200.0);

        testSaleDetail = new SaleDetailEntity();
        testSaleDetail.setId(1L);
        testSaleDetail.setSale(testSale);
        testSaleDetail.setProduct(testProduct);
        testSaleDetail.setQuantity(2);
        testSaleDetail.setUnitPrice(100.0);

        validRequest = CreateSaleRequest.builder().clientId(12345678L).products(List.of(
                new CreateSaleRequest.Product(1L, 2)
        )).build();
    }

    @Test
    void create_WithValidRequest_ShouldSaveSaleAndDetails() {
        // Arrange
        when(clientRepository.findByDocument(any())).thenReturn(Optional.of(testClient));
        when(productRepository.findById(any())).thenReturn(Optional.of(testProduct));
        when(saleRepository.save(any())).thenReturn(testSale);
        when(saleDetailRepository.save(any())).thenReturn(testSaleDetail);

        // Act
        saleService.create(validRequest);

        // Assert
        verify(saleRepository, times(1)).save(any(SaleEntity.class));
        verify(saleDetailRepository, atLeastOnce()).save(any(SaleDetailEntity.class));
        verify(productRepository, atLeastOnce()).save(any(ProductEntity.class));
    }

    @Test
    void create_WithInvalidClient_ShouldThrowException() {
        // Arrange
        when(clientRepository.findByDocument(any())).thenReturn(Optional.empty());
        CreateSaleRequest invalidRequest = CreateSaleRequest.builder().clientId(99999999L).build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> saleService.create(invalidRequest));
    }

    @Test
    void create_WithEmptyProducts_ShouldThrowException() {
        // Arrange
        CreateSaleRequest invalidRequest = CreateSaleRequest.builder()
                .clientId(12345678L).products(List.of()).build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> saleService.create(invalidRequest));
    }

    @Test
    void create_WithInsufficientStock_ShouldThrowException() {
        // Arrange
        testProduct.setQuantity(1); // Solo 1 disponible
        when(clientRepository.findByDocument(any())).thenReturn(Optional.of(testClient));
        when(productRepository.findById(any())).thenReturn(Optional.of(testProduct));
        when(saleRepository.save(any())).thenReturn(testSale);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> saleService.create(validRequest));
    }

    @Test
    void maxId_ShouldReturnFormattedId() {
        // Arrange
        when(saleRepository.getMaxId()).thenReturn(5L);

        // Act
        GetMaxIdResponse response = saleService.maxId();

        // Assert
        assertEquals("F0006", response.getId());
    }

    @Test
    void getSaleInfo_WithValidId_ShouldReturnSaleInfo() {
        // Arrange
        when(saleRepository.findById(1L)).thenReturn(Optional.of(testSale));
        when(saleDetailRepository.findBySale_Id(1L)).thenReturn(List.of(testSaleDetail));
        when(clientRepository.findByDocument(any())).thenReturn(Optional.of(testClient));

        // Act
        GetSaleInfo saleInfo = saleService.getSaleInfo(1L);

        // Assert
        assertNotNull(saleInfo);
        assertEquals("F001", saleInfo.getSerie());
        assertEquals(200.0, saleInfo.getSubTotal());
        assertEquals(236.0, saleInfo.getTotal()); // 200 + 18% IGV
        assertEquals(36.0, saleInfo.getIgv());
        assertEquals(1, saleInfo.getProducts().size());
        assertEquals("John Doe", saleInfo.getClientInfo().getName());
    }

    @Test
    void getSaleInfo_WithInvalidId_ShouldReturnNull() {
        // Arrange
        when(saleRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        GetSaleInfo saleInfo = saleService.getSaleInfo(999L);

        // Assert
        assertNull(saleInfo);
    }

    @Test
    void getAllSales_ShouldReturnListOfSales() {
        // Arrange
        when(saleRepository.findAll()).thenReturn(List.of(testSale));

        // Act
        List<GetAllSalesResponse> sales = saleService.getAllSales();

        // Assert
        assertEquals(1, sales.size());
        assertEquals("F001", sales.get(0).getSerie());
        assertEquals(200.0, sales.get(0).getTotalAmount());
    }

    @Test
    void calculateTotal_ShouldReturnCorrectSum() {
        // Arrange
        List<CreateSaleRequest.Product> products = List.of(
                new CreateSaleRequest.Product(1L, 2),
                new CreateSaleRequest.Product(2L, 1)
        );

        ProductEntity product2 = new ProductEntity();
        product2.setId(2L);
        product2.setPrice(50.0);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product2));

        // Act
        double total = saleService.calculateTotal(products);

        // Assert
        assertEquals(250.0, total); // (100 * 2) + (50 * 1)
    }

    @Test
    void updateProductStock_ShouldDecreaseQuantity() {
        // Arrange
        int initialQuantity = testProduct.getQuantity();
        int quantityToSell = 2;

        // Act
        saleService.updateProductStock(testProduct, quantityToSell);

        // Assert
        assertEquals(initialQuantity - quantityToSell, testProduct.getQuantity());
        verify(productRepository, times(1)).save(testProduct);
    }
}