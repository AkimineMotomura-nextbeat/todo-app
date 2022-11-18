import { Component, Input, OnInit } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute } from '@angular/router';

import { Category } from '../../models/category';
import { CategoryService } from '../../service/category.service';

@Component({
  selector: 'app-category-detail',
  templateUrl: './category-detail.component.html',
  styleUrls: ['./category-detail.component.css']
})
export class CategoryDetailComponent implements OnInit {

  @Input() category?: Category;

  constructor(
    private routes          : ActivatedRoute,
    private location        : Location,
    private categoryService : CategoryService
  ) { }

  ngOnInit(): void {
    this.getCategory();
  }

  goBack(): void {
    this.location.back();
  }

  getCategory(): void {
    const id = Number(this.routes.snapshot.paramMap.get('id'));
    this.categoryService.getCategory(id).subscribe(category => this.category = category)
  }

  save(): void {
    if (this.category) {
      this.categoryService.updateCategory(this.category)
        .subscribe(() => this.goBack());
    }
  }
}
