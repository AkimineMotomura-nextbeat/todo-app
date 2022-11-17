import { Component, OnInit } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute } from '@angular/router';

import { Category } from '../category';
import { CategoryService } from '../category.service';

@Component({
  selector: 'app-category-new',
  templateUrl: './category-new.component.html',
  styleUrls: ['./category-new.component.css']
})
export class CategoryNewComponent implements OnInit {

  category: Category = {id: 0, name: "", slug: "", color: 0}

  constructor(
    private routes      : ActivatedRoute,
    private location    : Location,
    private categoryService : CategoryService
  ) { }

  ngOnInit(): void {
  }

  goBack(): void {
    this.location.back();
  }

  save(): void {
    if (this.category) {
      this.categoryService.addCategory(this.category)
        .subscribe(() => this.goBack());
    }
  }
}
