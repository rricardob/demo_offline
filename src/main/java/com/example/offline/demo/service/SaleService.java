package com.example.offline.demo.service;

import com.example.offline.demo.controller.request.CreateSaleRequest;
import com.example.offline.demo.controller.response.GetAllSalesResponse;
import com.example.offline.demo.controller.response.GetMaxIdResponse;
import com.example.offline.demo.controller.response.GetSaleInfo;
import com.example.offline.demo.entity.ClientEntity;
import com.example.offline.demo.entity.ProductEntity;
import com.example.offline.demo.entity.SaleDetailEntity;
import com.example.offline.demo.entity.SaleEntity;
import com.example.offline.demo.repository.ClientRepository;
import com.example.offline.demo.repository.ProductRepository;
import com.example.offline.demo.repository.SaleDetailRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.offline.demo.repository.SaleRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final SaleDetailRepository saleDetailRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;


    @Transactional
    public void create(CreateSaleRequest request) {
        /*ClientEntity clientEntity = new ClientEntity();
        clientEntity.setId(request.getClientId());


        SaleEntity saleEntity = new SaleEntity();
        saleEntity.setClient(clientEntity);
        saleEntity.setSaleDate(LocalDate.now());*/

        // 1. Validar datos del request
        var x = clientRepository.findByDocument(request.getClientId());
        if (request.getClientId() == null || x.isEmpty()) {
            throw new IllegalArgumentException("Cliente inválido");
        }

        if (request.getProducts() == null || request.getProducts().isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener productos");
        }

        // 2. Crear la entidad Sale
        SaleEntity sale = new SaleEntity();
        sale.setClient(x.orElse(null)); // Obtener el cliente desde la BD
        sale.setSaleDate(LocalDate.now()); // Fecha actual
        sale.setTotalAmount(calculateTotal(request.getProducts())); // Calcular el total (ver método abajo)

        // 3. Guardar la venta (sin detalles aún)
        SaleEntity savedSale = saleRepository.save(sale);

        // 4. Guardar los detalles de la venta
        for (CreateSaleRequest.Product product : request.getProducts()) {
            ProductEntity productEntity = productRepository.findById(product.getId()).orElse(null);
            SaleDetailEntity saleDetail = new SaleDetailEntity();
            saleDetail.setSale(savedSale);
            saleDetail.setProduct(productEntity); // Obtener el producto
            saleDetail.setQuantity(product.getQuantity());
            saleDetail.setUnitPrice(saleDetail.getProduct().getPrice()); // Asumiendo que Product tiene un precio

            // Validar datos del detalle (¡también es crucial!)
            if (saleDetail.getProduct() == null) {
                throw new IllegalArgumentException("Producto inválido en el detalle de venta");
            }

            if (saleDetail.getQuantity() <= 0) {
                throw new IllegalArgumentException("Cantidad inválida en el detalle de venta");
            }

            saleDetailRepository.save(saleDetail);

            // Actualizar el stock del producto
            updateProductStock(productEntity, product.getQuantity());
        }

        //return savedSale;

    }

    public GetMaxIdResponse maxId() {
        long maxId = saleRepository.getMaxId();
        return GetMaxIdResponse.builder().id("F000" + (maxId + 1)).build();
    }

    // Método para calcular el total
    private double calculateTotal(List<CreateSaleRequest.Product> products) {
        double total = 0;
        for (CreateSaleRequest.Product product : products) {
            ProductEntity p = productRepository.findById(product.getId()).orElse(null);
            if (p != null) {
                total += p.getPrice() * product.getQuantity();
            }
        }
        return total;
    }

    // Método para actualizar el stock del producto
    private void updateProductStock(ProductEntity product, int quantity) {
        if (product != null) {
            int newStock = product.getQuantity() - quantity;
            if (newStock < 0) {
                throw new IllegalArgumentException("No hay suficiente stock para el producto " + product.getName());
            }
            product.setQuantity(newStock);
            productRepository.save(product);
        }
    }

    public GetSaleInfo getSaleInfo(long id) {
        SaleEntity sale = saleRepository.findById(id).orElse(null);
        if (sale == null) {
            return null; // O lanza una excepción si prefieres
        }

        List<SaleDetailEntity> saleDetails = saleDetailRepository.findBySale_Id(sale.getId());
        ClientEntity client = clientRepository.findByDocument(Long.parseLong(sale.getClient().getDocument().toString())).orElse(null);
        if (client == null) {
            return null; // O lanza una excepción si prefieres
        }

        GetSaleInfo.ClientInfo clientInfo = GetSaleInfo.ClientInfo.builder()
                .dni(client.getDocument())
                .name(client.getFirstName().concat(" ").concat(client.getLastName()))
                .build();

        List<GetSaleInfo.ProductsInfo> productsInfo = buildProductsInfo(saleDetails);

        double totalIgv = sale.getTotalAmount() * 0.18;

        return GetSaleInfo.builder()
                .clientInfo(clientInfo)
                .products(productsInfo)
                .saleDate(sale.getSaleDate())
                .serie("F00" + sale.getId())
                .subTotal(sale.getTotalAmount())
                .total(sale.getTotalAmount() + totalIgv)
                .igv(sale.getTotalAmount() * 0.18)
                .build();
    }

    private List<GetSaleInfo.ProductsInfo> buildProductsInfo(List<SaleDetailEntity> saleDetails) {
        List<GetSaleInfo.ProductsInfo> productsInfo = new ArrayList<>();
        for (SaleDetailEntity saleDetail : saleDetails) {
            ProductEntity product = saleDetail.getProduct(); // Asumiendo que SaleDetailEntity tiene una relación con ProductEntity
            if (product != null) {
                GetSaleInfo.ProductsInfo productInfo = GetSaleInfo.ProductsInfo.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .quantity(saleDetail.getQuantity())
                        .price(product.getPrice()) // Asumiendo que ProductEntity tiene un campo price
                        .subTotal(product.getPrice() * saleDetail.getQuantity())
                        .build();
                productsInfo.add(productInfo);
            }
        }
        return productsInfo;
    }

    public List<GetAllSalesResponse> getAllSales() {
        List<SaleEntity> sales = this.saleRepository.findAll();
        return sales.stream()
                .map(s ->
                        GetAllSalesResponse.builder()
                                .id(s.getId())
                                .saleDate(s.getSaleDate())
                                .totalAmount(s.getTotalAmount())
                                .serie("F00" + s.getId())
                                /*.client(GetAllSalesResponse.ClientInfo
                                        .builder()
                                        .id(s.getClient().getId())
                                        .document(s.getClient().getDocument())
                                        .email(s.getClient().getEmail())
                                        .firstName(s.getClient().getFirstName())
                                        .lastName(s.getClient().getLastName())
                                        .build())*/
                                .build()
                ).collect(Collectors.toList());
    }


}


