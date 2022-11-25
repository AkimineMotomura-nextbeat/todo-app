import { Component, OnInit } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormControl, FormGroup } from '@angular/forms';
import { Validators } from '@angular/forms';

import { Todo } from '../../models/todo';
import { Category } from 'src/app/models/category';
import { TodoService } from '../../service/todo.service';
import { CategoryService } from 'src/app/service/category.service';

@Component({
  selector: 'app-todo-new',
  templateUrl: './todo-new.component.html',
  styleUrls: ['./todo-new.component.css']
})
export class TodoNewComponent implements OnInit {
  
  todoForm: FormGroup;
  categoryList: Category[] = []

  constructor(
    private routes      : ActivatedRoute,
    private location    : Location,
    private todoService : TodoService,
    private categoryService: CategoryService
  ) { 
    this.todoForm = new FormGroup({
      title:    new FormControl('', Validators.required),
      content:  new FormControl(''),
      category: new FormControl(6)
    })
  }

  ngOnInit(): void {
    this,this.getCategoryList();
  }

  getCategoryList(): void {
    this.categoryService.getCategoryList().subscribe(
      categoryList => this.categoryList = categoryList
    )
  }

  goBack(): void {
    this.location.back();
  }

  save(): void {
    if(!this.titleForm.invalid){
      this.todoService.addTodo({
        id      : 0,
        category: Number(this.categoryForm.value),
        title   : this.titleForm.value,
        content : this.contentForm.value,
        state   : 0
      }).subscribe(() => this.goBack());
    }
  }

  get titleForm() {
    return this.todoForm.controls['title'];
  }

  get contentForm() {
    return this.todoForm.controls['content'];
  }

  get categoryForm() {
    return this.todoForm.controls['category'];
  }
}
