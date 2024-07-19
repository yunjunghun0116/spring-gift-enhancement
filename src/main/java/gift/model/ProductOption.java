package gift.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "product_option")
public class ProductOption extends BaseEntity {
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;
    @NotNull
    @Column(name = "name")
    private String name;
    @NotNull
    @Column(name = "quantity")
    private Integer quantity;

    protected ProductOption() {
    }

    public ProductOption(Product product, String name, Integer quantity) {
        this.product = product;
        product.getProductOptionList().add(this);
        this.name = name;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public String getName() {
        return name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void updateOptionInfo(String name, Integer quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public void subtract(Integer subQuantity) {
        this.quantity = this.quantity - subQuantity;
    }
}
