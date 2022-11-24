import { Component, OnInit } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormControl, FormGroup } from '@angular/forms';
import { Validators } from '@angular/forms';

import { Category } from '../../models/category';
import { CategoryService } from '../../service/category.service';
import { CategoryColor } from 'src/app/models/color';

@Component({
  selector: 'app-category-new',
  templateUrl: './category-new.component.html',
  styleUrls: ['./category-new.component.css']
})
export class CategoryNewComponent implements OnInit {

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
    this.getCategoryColorList();
  }

  getCategoryColorList(): void {
    this.categoryService.getCategoryColorList().subscribe(
      categoryColorList => this.categoryColorList = categoryColorList
    )
  }

  goBack(): void {
    this.location.back();
  }

  save(): void {
    this.categoryService.addCategory({
      id    : 0,
      name  : this.nameForm.value,
      slug  : this.slugForm.value,
      color : this.colorForm.value
    }).subscribe(() => this.goBack());
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
