import { Component, Input, OnInit } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormControl, FormGroup } from '@angular/forms';
import { Validators } from '@angular/forms';

import { Category } from '../../models/category';
import { CategoryService } from '../../service/category.service';
import { CategoryColor } from 'src/app/models/color';

@Component({
  selector: 'app-category-detail',
  templateUrl: './category-detail.component.html',
  styleUrls: ['./category-detail.component.css']
})
export class CategoryDetailComponent implements OnInit {

  @Input() category?: Category;

  categoryForm: FormGroup;
  categoryColorList: CategoryColor[] = [];

  constructor(
    private routes      : ActivatedRoute,
    private location    : Location,
    private categoryService : CategoryService
  ) {
    this.categoryForm = new FormGroup({
      name  : new FormControl('', Validators.required),
      slug  : new FormControl(''),
      color : new FormControl(0)
    })
  }

  ngOnInit(): void {
    this.getCategory();
    this.getCategoryColorList();
  }

  goBack(): void {
    this.location.back();
  }

  getCategory(): void {
    const id = Number(this.routes.snapshot.paramMap.get('id'));
    this.categoryService.getCategory(id).subscribe(category => {
      this.category = category;
      this.categoryForm.setValue({
        name:   this.category.name,
        slug:   this.category.slug,
        color:  this.category.color
      })
      console.log('x')
    })
  }

  getCategoryColorList(): void {
    this.categoryService.getCategoryColorList().subscribe(
      categoryColorList => this.categoryColorList = categoryColorList
    )
  }

  save(): void {
    if (this.category) {
      this.categoryService.updateCategory({
        id    : this.category.id,
        name  : this.nameForm.value,
        slug  : this.slugForm.value,
        color : Number(this.colorForm.value)
      }).subscribe(() => this.goBack());
    }
  }

  get nameForm() {
    return this.categoryForm.controls['name']
  }

  get slugForm() {
    return this.categoryForm.controls['slug']
  }

  get colorForm() {
    return this.categoryForm.controls['color']
  }
}
