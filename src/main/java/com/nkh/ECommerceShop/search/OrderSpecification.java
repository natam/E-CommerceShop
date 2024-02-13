package com.nkh.ECommerceShop.search;

import com.nkh.ECommerceShop.exception.NotValidInputException;
import com.nkh.ECommerceShop.model.order.Order;
import com.nkh.ECommerceShop.model.order.OrderStatus;
import com.nkh.ECommerceShop.model.order.OrderStatusHistory;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.*;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.*;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isAllBlank;

@Data
@Builder
public class OrderSpecification implements Specification<Order> {
    private String startDate;
    private String endDate;
    private String status;
    private Integer orderSum;
    private Long userId;
    @Override
    public Specification<Order> and(Specification<Order> other) {
        return Specification.super.and(other);
    }

    @Override
    public Specification<Order> or(Specification<Order> other) {
        return Specification.super.or(other);
    }

    @Override
    public Predicate toPredicate(Root<Order> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        Predicate creationDatePre = null;
        try {
            creationDatePre = careationDatePredicate(root, criteriaBuilder);
        } catch (Exception e) {
            throw new NotValidInputException("Not correct date input " + startDate);
        }
        Predicate orderSumPre = orderSum(root, criteriaBuilder);
        Predicate userIdPre = ofNullable(userId).map(b -> equals(criteriaBuilder, root.get("userId"), userId))
                .orElse(null);

        List<Predicate> predicates = new ArrayList<>();

        ofNullable(creationDatePre).ifPresent(predicates::add);
        ofNullable(orderSumPre).ifPresent(predicates::add);
        ofNullable(userIdPre).ifPresent(predicates::add);

        return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
    }

    private Predicate orderSum(Root<Order> root, CriteriaBuilder cb){
        if(isNull(orderSum)){
            return null;
        }
        int sumLow = Double.valueOf(orderSum * 0.9).intValue();
        int sumHigh = Double.valueOf(orderSum * 1.1).intValue();

        return cb.and(
                between(cb, root.get("totalOrderSum"), sumLow, sumHigh)
        );
    }

    private Predicate careationDatePredicate(Root<Order> root, CriteriaBuilder cb) throws Exception {
        if (isNull(startDate)&isNull(endDate)) {
            return null;
        }else if(nonNull(startDate)&nonNull(endDate)){
            return cb.and(
                    between(cb, root.get("createdAt"), stringToLocalDateTime(startDate), stringToLocalDateTime(endDate))
            );
        }else if(nonNull(startDate)){
            return cb.and(
                    greaterThanOrEqualTo(cb, root.get("createdAt"), stringToLocalDateTime(startDate))
            );
        }else {
            return cb.and(
                    lessThanOrEqualsTo(cb, root.get("createdAt"), stringToLocalDateTime(endDate))
            );
        }
    }

    private Predicate between(CriteriaBuilder cb, Path<LocalDateTime> field, LocalDateTime start, LocalDateTime end) {
        return cb.between(field, start, end);
    }

    private Predicate greaterThanOrEqualTo(CriteriaBuilder cb, Path<LocalDateTime> field, LocalDateTime startDate) {
        return cb.greaterThanOrEqualTo(field, startDate);
    }

    private Predicate like(CriteriaBuilder cb, Path<String> field, String searchTerm) {
        return cb.like(cb.lower(field), "%" + searchTerm.toLowerCase() + "%");
    }

    private Predicate lessThanOrEqualsTo(CriteriaBuilder cb, Path<LocalDateTime> field, LocalDateTime endDate) {
        return cb.lessThanOrEqualTo(field, endDate);
    }

    private Predicate equals(CriteriaBuilder cb, Path<Object> field, Object value) {
        return cb.equal(field, value);
    }

    private Predicate between(CriteriaBuilder cb, Path<Integer> field, int min, int max) {
        return cb.between(field, min, max);
    }

    private LocalDateTime stringToLocalDateTime(String strDate) throws DateTimeParseException {
            DateTimeFormatterBuilder dateTimeFormatterBuilder = new DateTimeFormatterBuilder()
                    .append(DateTimeFormatter.ofPattern("[MM/dd/yyyy]" + "[dd-MM-yyyy]" + "[yyyy-MM-dd]" +
                            "[dd.MM.yyyy]" + "[dd/MM/yyyy]" + "[yyyy.MM.dd]" + "[yyyy/MM/dd]" +
                            "[MM/dd/yyyy HH:mm:ssXXX]"));
            DateTimeFormatter dateTimeFormatter = dateTimeFormatterBuilder.toFormatter();
            LocalDate localDate = LocalDate.parse(strDate, dateTimeFormatter);
            return localDate.atStartOfDay();
    }

}
