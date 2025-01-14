

package org.cripsy.adminservice.service;

import org.cripsy.adminservice.dto.AdminDTO;
import org.cripsy.adminservice.dto.GetBestSellingDTO;
import org.cripsy.adminservice.model.Admin;
import org.cripsy.adminservice.repository.AdminRepository;
import jakarta.transaction.Transactional;
import org.cripsy.orderservice.dto.*;
import org.cripsy.productservice.dto.GetProductInfoDTO;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private ModelMapper modelMapper;

    private final WebClient webClient;

    public AdminService(WebClient webClient) {
        this.webClient = webClient;
    }


    public AdminDTO findAdminByUsername(String username) {
        //Customer customer = customerRepository.findUserByUsernameAndPassword(username,password);
        Admin admin = adminRepository.findAdminByName(username);
        if (admin != null) {
            AdminDTO dto = new AdminDTO();
            dto.setId(admin.getId());
            dto.setName(admin.getName());
            dto.setPassword(admin.getPassword());
            dto.setEmail(admin.getEmail());
            return dto;
        }
        return null;
    }

    // Save the Admin entity Service function
    public AdminDTO saveAdmin(AdminDTO adminDTO) {
        Admin admin = modelMapper.map(adminDTO, Admin.class);
        adminRepository.save(admin);
        return adminDTO; // Ensure we return the saved data in the expected format
    }

    // Read all Admins Service function
    public List<AdminDTO> getAllAdmins() {
        List<Admin> userList = adminRepository.findAll();
        return modelMapper.map(userList, new TypeToken<List<AdminDTO>>() {}.getType());
    }

    // Update Admin Service function
    public AdminDTO updateAdmin(AdminDTO adminDTO) {
        adminRepository.save(modelMapper.map(adminDTO, Admin.class));
        return adminDTO;
    }

    // Delete Admin Service function
    public AdminDTO deleteAdmin(AdminDTO adminDTO) {
        adminRepository.delete(modelMapper.map(adminDTO, Admin.class));
        return adminDTO;
    }

    // Find Admin by ID Service function
    public AdminDTO getAdminById(Integer id) {
        Optional<Admin> admin = adminRepository.findById(id);
        return modelMapper.map(admin.orElse(null), AdminDTO.class); // Avoiding null values
    }


    //Get Total Price Summary
    public List<AdminDashbordDTO> getMonthlySumTotal() {
        return webClient.get()
                .uri("http://localhost:8083/api/orders/getMonthlySumTotal")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<AdminDashbordDTO>>() {})
                .block();
    }

    ////Get Total Qty Summary
    public List<TotalItemDTO> getMonthlySumQty(){
        return webClient.get()
                .uri("http://localhost:8083/api/orders/getMonthlySumQty")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<TotalItemDTO>>() {})
                .block();
    }

    //Get Total Orders Summary
    public  List<TotalOrdersDTO> getOrderStats() {
         return webClient.get()
                .uri("http://localhost:8083/api/orders/orderSummery")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<TotalOrdersDTO>>() {})
                .block();

    }

    //Get Monthly Selling (Chart Data)
    public  List<MonthlyTotalPriceDTO> findMonthlyTotalPrices() {
        return webClient.get()
                .uri("http://localhost:8083/api/orders/monthly-selling")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<MonthlyTotalPriceDTO>>() {})
                .block();

    }


    //Get Total Customers
    public Long getTotalCustomers() {
        return webClient.get()
                .uri("http://localhost:8081/api/customers/total")
                .retrieve()
                .bodyToMono(Long.class)
                .block();
    }


    ////Get Best Selling Details
    public List<GetBestSellingDTO> getBestSellingProducts() {
        List<BestSellingProductDTO> bestSellingProductDTOList = webClient.get()
                .uri("http://localhost:8083/api/orders/best-selling")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<BestSellingProductDTO>>() {})
                .block();

        List<Integer> productIdList = bestSellingProductDTOList.stream().map(item -> item.getProductId()).toList();

        List<GetProductInfoDTO> productInfoDTOS = webClient.post()
                .uri("http://localhost:8082/api/product/getInfo")
                .bodyValue(productIdList)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<GetProductInfoDTO>>() {})
                .block();


        return bestSellingProductDTOList.stream()
                .map(bestSellingProduct -> {
                    GetProductInfoDTO productInfo = productInfoDTOS.stream()
                            .filter(info -> info.getProductId() == bestSellingProduct.getProductId())
                            .findFirst()
                            .orElse(null);

                    if (productInfo != null) {
                        return new GetBestSellingDTO(
                                bestSellingProduct.getProductId(),
                                bestSellingProduct.getTotalQuantity(),
                                bestSellingProduct.getTotalPrice(),
                                bestSellingProduct.getTotalDiscountedPrice(),
                                productInfo.getName(),
                                productInfo.getAvgRatings()
                        );
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }







}
