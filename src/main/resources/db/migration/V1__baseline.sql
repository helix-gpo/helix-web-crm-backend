
    create table employee_project_assignments (
        created_at datetime(6) not null,
        updated_at datetime(6) not null,
        version bigint not null,
        employee_id binary(16) not null,
        id binary(16) not null,
        project_id binary(16) not null,
        tenant_id binary(16) not null,
        created_by varchar(254),
        updated_by varchar(254),
        primary key (id)
    ) engine=InnoDB;

    create table employees (
        active bit not null,
        created_at datetime(6) not null,
        updated_at datetime(6) not null,
        version bigint not null,
        id binary(16) not null,
        role_id binary(16) not null,
        cognito_sub varchar(64),
        first_name varchar(100),
        last_name varchar(100),
        created_by varchar(254),
        email varchar(254) not null,
        updated_by varchar(254),
        primary key (id)
    ) engine=InnoDB;

    create table event_publication (
        completion_attempts integer not null,
        completion_date datetime(6),
        last_resubmission_date datetime(6),
        publication_date datetime(6) not null,
        id binary(16) not null,
        event_type varchar(255) not null,
        listener_id varchar(255) not null,
        serialized_event varchar(255) not null,
        status enum ('COMPLETED','FAILED','PROCESSING','PUBLISHED','RESUBMITTED'),
        primary key (id)
    ) engine=InnoDB;

    create table invoice_line_items (
        position_number integer not null,
        quantity decimal(10,2) not null,
        tax_rate_percentage decimal(5,2) not null,
        unit_price_amount decimal(38,2) not null,
        created_at datetime(6) not null,
        updated_at datetime(6) not null,
        version bigint not null,
        unit_code varchar(10) not null,
        id binary(16) not null,
        invoice_id binary(16) not null,
        milestone_id binary(16),
        created_by varchar(254),
        updated_by varchar(254),
        description varchar(300) not null,
        unit_price_currency varchar(255) not null,
        source enum ('CUSTOM','MILESTONE') not null,
        primary key (id)
    ) engine=InnoDB;

    create table invoice_sequences (
        sequence_year integer not null,
        current_value bigint not null,
        primary key (sequence_year)
    ) engine=InnoDB;

    create table invoices (
        currency_code varchar(3) not null,
        due_date date,
        issue_date date,
        paid_date date,
        payment_terms_days integer,
        created_at datetime(6) not null,
        issued_at datetime(6),
        sent_at datetime(6),
        updated_at datetime(6) not null,
        version bigint not null,
        id binary(16) not null,
        project_id binary(16),
        tenant_id binary(16) not null,
        invoice_number varchar(40),
        buyer_reference varchar(60),
        document_key varchar(200),
        created_by varchar(254),
        issued_by varchar(254),
        updated_by varchar(254),
        sent_to_email varchar(320),
        buyer_bic varchar(255),
        buyer_city varchar(255),
        buyer_country_code varchar(255),
        buyer_email varchar(255),
        buyer_house_number varchar(255),
        buyer_iban varchar(255),
        buyer_name varchar(255),
        buyer_postal_code varchar(255),
        buyer_street varchar(255),
        buyer_vat_id varchar(255),
        seller_bic varchar(255),
        seller_city varchar(255),
        seller_country_code varchar(255),
        seller_email varchar(255),
        seller_house_number varchar(255),
        seller_iban varchar(255),
        seller_name varchar(255),
        seller_postal_code varchar(255),
        seller_street varchar(255),
        seller_vat_id varchar(255),
        status enum ('CANCELLED','DRAFT','ISSUED','OVERDUE','PAID','SENT') not null,
        primary key (id)
    ) engine=InnoDB;

    create table milestones (
        amount decimal(19,2),
        currency_code varchar(3),
        due_date date,
        created_at datetime(6) not null,
        updated_at datetime(6) not null,
        version bigint not null,
        id binary(16) not null,
        project_id binary(16) not null,
        title varchar(160) not null,
        created_by varchar(254),
        updated_by varchar(254),
        description varchar(500),
        status enum ('DONE','IN_PROGRESS','PLANNED') not null,
        primary key (id)
    ) engine=InnoDB;

    create table project_highlights (
        sort_order integer not null check ((sort_order>=0)),
        project_id binary(16) not null,
        highlight varchar(300),
        primary key (sort_order, project_id)
    ) engine=InnoDB;

    create table project_tags (
        tag_color varchar(7),
        project_id binary(16) not null,
        tag_value varchar(60)
    ) engine=InnoDB;

    create table projects (
        end_date date,
        start_date date,
        visible_on_website bit not null,
        created_at datetime(6) not null,
        updated_at datetime(6) not null,
        version bigint not null,
        id binary(16) not null,
        tenant_id binary(16) not null,
        title varchar(160) not null,
        created_by varchar(254),
        updated_by varchar(254),
        description varchar(500),
        image_key varchar(500),
        notes TEXT,
        full_description TEXT,
        status enum ('CANCELLED','COMPLETED','IN_PROGRESS','LEAD','ON_HOLD') not null,
        primary key (id)
    ) engine=InnoDB;

    create table role_permissions (
        role_id binary(16) not null,
        action enum ('DELETE','READ','WRITE'),
        entity_type enum ('INVOICE','MILESTONE','PARTNER','PROJECT','TENANT','TESTIMONIAL')
    ) engine=InnoDB;

    create table roles (
        unrestricted bit not null,
        created_at datetime(6) not null,
        updated_at datetime(6) not null,
        version bigint not null,
        id binary(16) not null,
        name varchar(60) not null,
        created_by varchar(254),
        updated_by varchar(254),
        description varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table tenant_partners (
        created_at datetime(6) not null,
        updated_at datetime(6) not null,
        version bigint not null,
        id binary(16) not null,
        tenant_id binary(16) not null,
        phone varchar(30),
        first_name varchar(100) not null,
        last_name varchar(100) not null,
        role varchar(100),
        created_by varchar(254),
        email varchar(254),
        updated_by varchar(254),
        photo_key varchar(500),
        primary key (id)
    ) engine=InnoDB;

    create table tenants (
        visible_on_website bit not null,
        created_at datetime(6) not null,
        updated_at datetime(6) not null,
        version bigint not null,
        reference_code varchar(12),
        id binary(16) not null,
        vat_id varchar(20),
        contact_phone varchar(30),
        company_name varchar(160) not null,
        legal_name varchar(160),
        contact_email varchar(254),
        created_by varchar(254),
        updated_by varchar(254),
        logo_key varchar(500),
        website_url varchar(500),
        city varchar(255),
        country_code varchar(255),
        house_number varchar(255),
        notes TEXT,
        postal_code varchar(255),
        street varchar(255),
        status enum ('ACTIVE','ARCHIVED','INACTIVE','PROSPECT') not null,
        primary key (id)
    ) engine=InnoDB;

    create table testimonial_invitations (
        created_at datetime(6) not null,
        expires_at datetime(6) not null,
        sent_at datetime(6),
        updated_at datetime(6) not null,
        used_at datetime(6),
        version bigint not null,
        id binary(16) not null,
        partner_id binary(16) not null,
        project_id binary(16),
        tenant_id binary(16) not null,
        token_hash varchar(64) not null,
        created_by varchar(254),
        sent_to_email varchar(254),
        updated_by varchar(254),
        status enum ('EXPIRED','PENDING','REVOKED','USED') not null,
        primary key (id)
    ) engine=InnoDB;

    create table testimonials (
        rating integer not null,
        visible_on_website bit not null,
        created_at datetime(6) not null,
        updated_at datetime(6) not null,
        version bigint not null,
        id binary(16) not null,
        invitation_id binary(16) not null,
        partner_id binary(16) not null,
        project_id binary(16),
        tenant_id binary(16) not null,
        partner_role_snapshot varchar(100),
        company_name_snapshot varchar(160) not null,
        partner_name_snapshot varchar(200) not null,
        created_by varchar(254),
        updated_by varchar(254),
        description varchar(1000) not null,
        status enum ('APPROVED','PENDING_REVIEW','REJECTED') not null,
        primary key (id)
    ) engine=InnoDB;

    alter table employees 
       add constraint UKj9xgmd0ya5jmus09o0b8pqrpb unique (email);

    alter table invoices 
       add constraint UKl1x55mfsay7co0r3m9ynvipd5 unique (invoice_number);

    alter table role_permissions 
       add constraint UKjber9g1klc2w2ryig7b9mk1ny unique (role_id, entity_type, action);

    alter table roles 
       add constraint UKofx66keruapi6vyqpv6f2or37 unique (name);

    alter table testimonial_invitations 
       add constraint UKhbk77a5692q3ys5h5esn89s69 unique (token_hash);

    alter table employees 
       add constraint FKah490190ww1q2a4piuv41hk6e 
       foreign key (role_id) 
       references roles (id);

    alter table invoice_line_items 
       add constraint fk_line_item_invoice 
       foreign key (invoice_id) 
       references invoices (id);

    alter table milestones 
       add constraint fk_milestone_project 
       foreign key (project_id) 
       references projects (id);

    alter table project_highlights 
       add constraint FKm2xvw0l8n5l70hn57egfpqcpa 
       foreign key (project_id) 
       references projects (id);

    alter table project_tags 
       add constraint FKra1vi3p19o2pqtm3c1geaose9 
       foreign key (project_id) 
       references projects (id);

    alter table role_permissions 
       add constraint FKn5fotdgk8d1xvo8nav9uv3muc 
       foreign key (role_id) 
       references roles (id);

    alter table tenant_partners 
       add constraint fk_partner_tenant 
       foreign key (tenant_id) 
       references tenants (id);
