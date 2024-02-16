package com.nkh.ECommerceShop.search;

import com.nkh.ECommerceShop.model.Product;
import jakarta.persistence.criteria.*;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

@Data
@Builder
public class ProductSpecification implements Specification<Product> {
    private String name;
    private Double priceStart;
    private Double priceEnd;
    private int stockAvailability;
    @Override
    public Specification<Product> and(Specification<Product> other) {
        return Specification.super.and(other);
    }

    @Override
    public Specification<Product> or(Specification<Product> other) {
        return Specification.super.or(other);
    }

    @Override
    public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        Predicate creationDatePre = priceRangePredicate(root, criteriaBuilder);
        Predicate orderSumPre = stockAvailabilityPredicate(root, criteriaBuilder);
        Predicate namePre = ofNullable(name).map(b -> like(criteriaBuilder, root.get("name"), name))
                .orElse(null);

        List<Predicate> predicates = new ArrayList<>();

        ofNullable(creationDatePre).ifPresent(predicates::add);
        ofNullable(orderSumPre).ifPresent(predicates::add);
        ofNullable(namePre).ifPresent(predicates::add);

        return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
    }

    private Predicate stockAvailabilityPredicate(Root<Product> root, CriteriaBuilder cb){
        if(stockAvailability<=0){
            return null;
        }
        return cb.and(
                greaterThanOrEqualIntTo(cb, root.get("stock"), stockAvailability)
        );
    }

    private Predicate priceRangePredicate(Root<Product> root, CriteriaBuilder cb){
        if (isNull(priceStart)&isNull(priceEnd)) {
            return null;
        }else if(nonNull(priceStart)&nonNull(priceEnd)){
            return cb.and(
                    between(cb, root.get("price"), priceStart, priceEnd)
            );
        }else if(nonNull(priceStart)){
            return cb.and(
                    greaterThanOrEqualTo(cb, root.get("price"), priceStart)
            );
        }else {
            return cb.and(
                    lessThanOrEqualsTo(cb, root.get("price"), priceEnd)
            );
        }
    }

    private Predicate between(CriteriaBuilder cb, Path<Double> field, double start, double end) {
        return cb.between(field, start, end);
    }

    private Predicate greaterThanOrEqualIntTo(CriteriaBuilder cb, Path<Integer> field, int stock) {
        return cb.greaterThanOrEqualTo(field, stock);
    }

    private Predicate greaterThanOrEqualTo(CriteriaBuilder cb, Path<Double> field, double price) {
        return cb.greaterThanOrEqualTo(field, price);
    }

    private Predicate like(CriteriaBuilder cb, Path<String> field, String searchTerm) {
        return cb.like(cb.lower(field), "%" + searchTerm.toLowerCase() + "%");
    }

    private Predicate lessThanOrEqualsTo(CriteriaBuilder cb, Path<Double> field, double priceEnd) {
        return cb.lessThanOrEqualTo(field, priceEnd);
    }
}
