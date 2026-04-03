package com.HotelBook.HotelBooking.room;




import jakarta.validation.constraints.*;
import java.math.BigDecimal;


public class RoomRequestDTO {

    @NotBlank(message = "Room name is required")
    @Size(max = 100, message = "Room name cannot exceed 100 characters")
    private String name;

    @NotNull(message = "Room type is required")
    private RoomType type;

    @NotNull(message = "Bed type is required")
    private BedType bedType;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @NotNull(message = "Max adults is required")
    @Min(value = 1, message = "Room must allow at least 1 adult")
    @Max(value = 20, message = "Max adults cannot exceed 20")
    private Integer maxAdults;

    @NotNull(message = "Max children is required")
    @Min(value = 0, message = "Max children cannot be negative")
    @Max(value = 10, message = "Max children cannot exceed 10")
    private Integer maxChildren;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 500, message = "Quantity cannot exceed 500")
    private Integer quantity;

    @DecimalMin(value = "1.00", message = "Size must be at least 1 sqm")
    private BigDecimal sizeSqm;

    private Integer floor;

    private RoomView view;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;



    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public RoomType getType() { return type; }
    public void setType(RoomType type) { this.type = type; }

    public BedType getBedType() { return bedType; }
    public void setBedType(BedType bedType) { this.bedType = bedType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getMaxAdults() { return maxAdults; }
    public void setMaxAdults(Integer maxAdults) { this.maxAdults = maxAdults; }

    public Integer getMaxChildren() { return maxChildren; }
    public void setMaxChildren(Integer maxChildren) { this.maxChildren = maxChildren; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getSizeSqm() { return sizeSqm; }
    public void setSizeSqm(BigDecimal sizeSqm) { this.sizeSqm = sizeSqm; }

    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }

    public RoomView getView() { return view; }
    public void setView(RoomView view) { this.view = view; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
